package org.devocative.adroit.sql;

import org.devocative.adroit.sql.plugin.INpsPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedParameterStatement {
	private static final Logger logger = LoggerFactory.getLogger(NamedParameterStatement.class);

	/*
	Patterns to ignore:
		(['].*?[']) ignore characters between two single quote (SQL string constant)
		(["].*?["]) ignore characters between two double quote (SQL identifier)
		(--.*?\n) ignore characters in single line comments
		(/[*].*?[*]/) ignore characters in inline comments
		(extract[(].+?[)]) ignore characters in extract() function: it has "from" in its syntax e.g. extract(day from ?)

	Main Patterns:
		[:]([\w\d_]+) finding parameter without ':'
		(from|join|into|update)[\s]+(\w+([.]\w+)?) finding table name with schema if mentioned
	*/
	private static final Pattern PARAM_PATTERN = Pattern.compile("(['].*?['])|(--.*?\\n)|(/[*].*?[*]/)|[:]([\\w\\d_]+)");
	private static final Pattern PARAM_Q_MARK_PATTERN = Pattern.compile("(['].*?['])|(--.*?\\n)|(/[*].*?[*]/)|([?])");

	// ------------------------------

	private Map<String, List<Integer>> paramsPlacement = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private Map<String, Object> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private Map<Integer, Object> finalParams = new LinkedHashMap<>();

	private boolean hasBatch = false;
	private boolean ignoreExtraPassedParam = false, ignoreMissedParam = false;
	private Class<? extends Date> dateClassReplacement;
	private String query;
	private String finalQuery;
	private String finalIndexedQuery;
	private String id;

	private Connection connection;
	private PreparedStatement preparedStatement;

	private int batchSizeToFlush = 10000;
	private long totalBatchCount = 0, batchCount = 0;
	private Integer fetchSize, queryTimeout, maxRows;

	private List<INpsPlugin> plugins = new ArrayList<>();

	// ------------------------------ CONSTRUCTORS

	public NamedParameterStatement(Connection connection) {
		this(connection, null);
	}

	// Main Constructor
	public NamedParameterStatement(Connection connection, String query) {
		this.connection = connection;
		this.query = query;
	}

	// ------------------------------ UTIL METHODS

	public static List<String> findParamsInQuery(String query, boolean changeToLower) {
		List<String> result = new ArrayList<>();

		Matcher matcher = PARAM_PATTERN.matcher(query);

		while (matcher.find()) {
			if (matcher.group(4) != null) {
				String param;
				if (changeToLower) {
					param = matcher.group(4).toLowerCase();
				} else {
					param = matcher.group(4);
				}

				if (!result.contains(param)) {
					result.add(param);
				}
			}
		}

		return result;
	}

	// ------------------------------ ACCESSORS

	public String getQuery() {
		return query;
	}

	public NamedParameterStatement setQuery(String query) {
		this.query = query;
		return this;
	}

	public String getId() {
		return id;
	}

	public NamedParameterStatement setId(String id) {
		this.id = id;
		return this;
	}

	public NamedParameterStatement setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
		return this;
	}

	public NamedParameterStatement setQueryTimeout(int timeoutInSeconds) {
		this.queryTimeout = timeoutInSeconds;
		return this;
	}

	public NamedParameterStatement setMaxRows(Integer maxRows) {
		this.maxRows = maxRows;
		return this;
	}

	public long getTotalBatchCount() {
		return totalBatchCount;
	}

	public int getBatchSizeToFlush() {
		return batchSizeToFlush;
	}

	public NamedParameterStatement setBatchSizeToFlush(int batchSizeToFlush) {
		this.batchSizeToFlush = batchSizeToFlush;
		return this;
	}

	public NamedParameterStatement setDateClassReplacement(Class<? extends Date> dateClassReplacement) {
		this.dateClassReplacement = dateClassReplacement;
		return this;
	}

	public String getFinalQuery() {
		return finalQuery;
	}

	public String getFinalIndexedQuery() {
		return finalIndexedQuery;
	}

	public Map<Integer, Object> getFinalParams() {
		return finalParams;
	}

	public NamedParameterStatement addOrReplaceParameter(String name, Object value) {
		params.put(name, value);
		return this;
	}

	public NamedParameterStatement setParameter(String name, Object value) {
		params.put(name, value);
		return this;
	}

	public NamedParameterStatement setParameters(Map<String, Object> params) {
		if (params != null) {
			for (Map.Entry<String, Object> param : params.entrySet()) {
				this.params.put(param.getKey(), param.getValue());
			}
		}
		return this;
	}

	public Map<String, Object> getParams() {
		return new HashMap<>(params);
	}

	public Connection getConnection() {
		return connection;
	}

	public NamedParameterStatement setIgnoreExtraPassedParam(boolean ignoreExtraPassedParam) {
		this.ignoreExtraPassedParam = ignoreExtraPassedParam;
		return this;
	}

	public NamedParameterStatement setIgnoreMissedParam(boolean ignoreMissedParam) {
		this.ignoreMissedParam = ignoreMissedParam;
		return this;
	}

	// ------------------------------ PUBLIC METHODS

	public NamedParameterStatement addPlugin(INpsPlugin plugin) {
		plugins.add(plugin);
		return this;
	}

	public ResultSet executeQuery() throws SQLException {
		processQuery();
		applyAllParams();
		return preparedStatement.executeQuery();
	}

	public int executeUpdate() throws SQLException {
		processQuery();
		applyAllParams();
		return preparedStatement.executeUpdate();
	}

	public boolean execute() throws SQLException {
		processQuery();
		applyAllParams();
		return preparedStatement.execute();
	}

	public void addBatch() throws SQLException {
		processQuery();
		applyAllParams();

		preparedStatement.addBatch();
		totalBatchCount++;
		batchCount++;
		hasBatch = true;

		if (batchSizeToFlush > 0 && totalBatchCount % batchSizeToFlush == 0) {
			executeBatch();
		}
	}

	public boolean hasBatch() {
		return hasBatch;
	}

	public int[] executeBatch() throws SQLException {
		int[] result = null;

		processQuery();
		if (hasBatch) {
			result = preparedStatement.executeBatch();
		}
		hasBatch = false;
		batchCount = 0;
		logger.debug("Execute Batch [{}], count={}", id, batchCount);

		return result;
	}

	public ResultSet getResultSet() throws SQLException {
		return preparedStatement.getResultSet();
	}

	public int getUpdateCount() throws SQLException {
		return preparedStatement.getUpdateCount();
	}

	public void close() throws SQLException {
		if (preparedStatement != null && !preparedStatement.isClosed()) {
			preparedStatement.close();
		}
	}

	public void cancel() throws SQLException {
		if (preparedStatement != null && !preparedStatement.isClosed()) {
			preparedStatement.cancel();
		}
	}

	// ------------------------------ PRIVATE METHODS

	private void processQuery() throws SQLException {
		if (preparedStatement != null) {
			return;
		}

		if (query == null) {
			throw new RuntimeException("Invalid NamedParameterStatement: no query!");
		}

		logger.debug("Orig Query: {}", query);
		StringBuffer builder = new StringBuffer();

		String queryByPlugin = query;
		for (INpsPlugin plugin : plugins) {
			queryByPlugin = plugin.process(queryByPlugin, params);
			if (logger.isDebugEnabled()) {
				logger.debug("Apply plugin: name=[{}] query=[{}] params={}",
					plugin.getClass().getSimpleName(), queryByPlugin, params);
			}
		}

		Matcher matcher = PARAM_PATTERN.matcher(queryByPlugin);
		int noOfParams = 0;
		while (matcher.find()) {
			if (matcher.group(4) != null) {
				noOfParams++;
				String param = matcher.group(4);
				logger.debug("Param: {}", param);
				if (!paramsPlacement.containsKey(param)) {
					paramsPlacement.put(param, new ArrayList<>());
				}
				paramsPlacement.get(param).add(noOfParams);

				Object paramValue = params.get(param);
				StringBuilder paramReplacementBuilder = new StringBuilder("?");
				if (paramValue != null && (paramValue instanceof Collection || paramValue.getClass().isArray())) {
					int size = paramValue instanceof Collection ?
						((Collection) paramValue).size() :
						((Object[]) paramValue).length;
					for (int i = 1; i < size; i++) {
						paramReplacementBuilder.append(",?");
					}
				}
				matcher.appendReplacement(builder, paramReplacementBuilder.toString());
			}
		}
		matcher.appendTail(builder);
		finalQuery = builder.toString();

		logger.debug("Final SQL: {}", finalQuery);
		logger.debug("Number of params: {}", noOfParams);

		if (!ignoreExtraPassedParam) {
			for (String param : params.keySet()) {
				if (!paramsPlacement.containsKey(param)) {
					throw new SQLException("Passed parameter not found: " + param);
				}
			}
		}

		preparedStatement = connection.prepareStatement(finalQuery);
		if (fetchSize != null) {
			preparedStatement.setFetchSize(fetchSize);
		}

		if (queryTimeout != null) {
			preparedStatement.setQueryTimeout(queryTimeout);
		}

		if (maxRows != null) {
			preparedStatement.setMaxRows(maxRows);
		}

		builder = new StringBuffer();
		matcher = PARAM_Q_MARK_PATTERN.matcher(finalQuery);
		int idx = 1;
		while (matcher.find()) {
			if (matcher.group(4) != null) {
				String replacement = String.format(" ?%s ", idx++);
				matcher.appendReplacement(builder, replacement);
			}
		}
		matcher.appendTail(builder);

		finalIndexedQuery = builder.toString();
	}

	private void applyAllParams() throws SQLException {
		finalParams.clear();

		// Validate all params existence
		List<String> missedParams = new ArrayList<>();
		for (String param : paramsPlacement.keySet()) {
			if (!params.containsKey(param)) {
				if (ignoreMissedParam) {
					params.put(param, null);
				} else {
					missedParams.add(param);
				}
			}
		}

		if (missedParams.size() > 0) {
			throw new RuntimeException(String.format("Parameter(s) missed for query %s: %s", id, missedParams));
		}

		TreeMap<Integer, Object> paramsByPlace = new TreeMap<>();
		for (Map.Entry<String, List<Integer>> entry : paramsPlacement.entrySet()) {
			for (Integer position : entry.getValue()) {
				paramsByPlace.put(position, params.get(entry.getKey()));
			}
		}

		/*for (Map.Entry<String, Object> paramsEntry : params.entrySet()) {
			List<Integer> positions = paramsPlacement.get(paramsEntry.getKey());
			for (Integer position : positions) {
				paramsByPlace.put(position, paramsEntry.getValue());
			}
		}*/

		int paramIndex = 1;

		for (Map.Entry<Integer, Object> paramEntry : paramsByPlace.entrySet()) {
			Object val = paramEntry.getValue();
			if (val != null && (val instanceof Collection || val.getClass().isArray())) {
				Iterator it = val instanceof Collection ?
					((Collection) val).iterator() :
					Arrays.asList(((Object[]) val)).iterator();

				while (it.hasNext()) {
					addValToPS(paramIndex, it.next());
					paramIndex++;
				}
			} else {
				addValToPS(paramIndex, val);
				paramIndex++;
			}
		}

		params.clear();
	}

	private void addValToPS(int index, Object val) throws SQLException {
		if (val != null && val.getClass().equals(Date.class)) {
			if (dateClassReplacement != null) {
				Date dt = (Date) val;
				try {
					Constructor<? extends Date> constructor = dateClassReplacement.getDeclaredConstructor(long.class);
					val = constructor.newInstance(dt.getTime());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				logger.warn("NPS param=[{}] has java.util.Date value, you can call setDateClassReplacement(<new class>)", index);
			}
		}
		preparedStatement.setObject(index, val);
		finalParams.put(index, val);
	}
}
