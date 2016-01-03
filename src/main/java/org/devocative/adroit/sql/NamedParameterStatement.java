package org.devocative.adroit.sql;

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
	private static final Set<String> KEYWORDS = new HashSet<>();

	static {
		KEYWORDS.add("set");
		KEYWORDS.add("dual");
	}

	private Map<String, List<Integer>> paramsPlacement = new HashMap<>();
	private Map<String, Object> params = new HashMap<>();

	private boolean hasBatch = false;
	private Class<? extends Date> dateClassReplacement;
	private String query, finalQuery, schema, id;

	private Connection connection;
	private PreparedStatement preparedStatement;

	private int batchSizeToFlush = 10000;
	private long totalBatchCount = 0, batchCount = 0;

	public NamedParameterStatement(Connection connection) {
		this(connection, null, null);
	}

	public NamedParameterStatement(Connection connection, String query) {
		this(connection, query, null);
	}

	// Main Constructor
	public NamedParameterStatement(Connection connection, String query, String schema) {
		this.connection = connection;
		this.query = query;
		this.schema = schema;
	}

	public static String applySchema(String schema, String query) {
		StringBuffer builder = new StringBuffer();
		Pattern p = Pattern.compile("(from|join|into|update)[\\s]+(\\w+(\\.\\w+)?)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = p.matcher(query);
		while (matcher.find()) {
			String rplc;
			if (matcher.group(2).contains(".") || KEYWORDS.contains(matcher.group(2).toLowerCase()))
				rplc = String.format("%s %s", matcher.group(1), matcher.group(2));
			else
				rplc = String.format("%s %s.%s", matcher.group(1), schema, matcher.group(2));
			matcher.appendReplacement(builder, rplc);
		}
		matcher.appendTail(builder);
		return builder.toString();
	}

	// ------------------- setters & getters -------------------

	public String getQuery() {
		return query;
	}

	public NamedParameterStatement setQuery(String query) {
		this.query = query;
		return this;
	}

	public String getSchema() {
		return schema;
	}

	public NamedParameterStatement setSchema(String schema) {
		this.schema = schema;
		return this;
	}

	public String getId() {
		return id;
	}

	public NamedParameterStatement setId(String id) {
		this.id = id;
		return this;
	}

	public Integer getFetchSize() throws SQLException {
		return preparedStatement.getFetchSize();
	}

	public NamedParameterStatement setFetchSize(int fetchSize) throws SQLException {
		preparedStatement.setFetchSize(fetchSize);
		return this;
	}

	public NamedParameterStatement setQueryTimeout(int timeoutInSeconds) throws SQLException {
		preparedStatement.setQueryTimeout(timeoutInSeconds);
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

	public NamedParameterStatement addOrReplaceParameter(String name, Object value) {
		params.put(name.toLowerCase(), value);
		return this;
	}

	public NamedParameterStatement setParameter(String name, Object value) {
		params.put(name.toLowerCase(), value);
		return this;
	}

	public NamedParameterStatement setParameters(Map<String, Object> params) {
		for (Map.Entry<String, Object> param : params.entrySet()) {
			this.params.put(param.getKey().toLowerCase(), param.getValue());
		}
		return this;
	}

	public Map<String, Object> getParams() {
		return new HashMap<>(params);
	}

	public Connection getConnection() {
		return connection;
	}

	// ------------------- Methods -------------------

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

	public void executeBatch() throws SQLException {
		processQuery();
		if (hasBatch) {
			preparedStatement.executeBatch();
		}
		hasBatch = false;
		batchCount = 0;
		logger.debug("Execute Batch [{}], count={}", id, batchCount);
	}

	public void close() throws SQLException {
		processQuery();
		preparedStatement.close();
	}

	private void processQuery() throws SQLException {
		if (preparedStatement != null) {
			return;
		}

		if (query == null) {
			throw new RuntimeException("Invalid NamedParameterStatement: no query!");
		}

		logger.debug("Orig Query: {}", query);
		StringBuffer builder = new StringBuffer();

		// Pattern to find parameters in the sql;
		//    (['].*?[']) try to ignore characters between two quote
		//    [:]([\w\d_]+) try to find parameter without :
		Pattern p = Pattern.compile("(['].*?['])|[:]([\\w\\d_]+)");
		Matcher matcher = p.matcher(query);
		int noOfParams = 0;
		while (matcher.find()) {
			if (matcher.group(1) == null) { // the group enclosing characters in single quotes is null
				noOfParams++;
				String param = matcher.group(2).toLowerCase();
				logger.debug("Param: {}", param);
				if (!paramsPlacement.containsKey(param)) {
					paramsPlacement.put(param, new ArrayList<Integer>());
				}
				paramsPlacement.get(param).add(noOfParams);

				Object paramValue = params.get(param);
				StringBuilder paramReplacementBuilder = new StringBuilder("?");
				if (paramValue instanceof Collection || paramValue.getClass().isArray()) {
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

		if (schema != null && schema.length() > 0) {
			finalQuery = applySchema(schema, finalQuery);
		}

		logger.debug("Final SQL: {}", finalQuery);
		logger.debug("Number of params: {}", noOfParams);

		for (String param : params.keySet()) {
			if (!paramsPlacement.containsKey(param)) {
				throw new SQLException("Parameter not found: " + param);
			}
		}

		preparedStatement = connection.prepareStatement(finalQuery);
	}

	private void applyAllParams() throws SQLException {
		// Validate all params existence
		List<String> missedParams = new ArrayList<>();
		for (String param : paramsPlacement.keySet()) {
			if (!params.containsKey(param)) {
				missedParams.add(param);
			}
		}

		if (missedParams.size() > 0) {
			throw new RuntimeException(String.format("Parameter(s) missed for query %s: %s", id, missedParams));
		}

		TreeMap<Integer, Object> paramsByPlace = new TreeMap<>();
		for (Map.Entry<String, Object> paramsEntry : params.entrySet()) {
			List<Integer> positions = paramsPlacement.get(paramsEntry.getKey());
			for (Integer position : positions) {
				paramsByPlace.put(position, paramsEntry.getValue());
			}
		}

		int paramIndex = 1;

		for (Map.Entry<Integer, Object> paramEntry : paramsByPlace.entrySet()) {
			Object val = paramEntry.getValue();
			if (val instanceof Collection || val.getClass().isArray()) {
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
		if (val.getClass().equals(Date.class)) {
			if (dateClassReplacement != null) {
				Date dt = (Date) val;
				try {
					Constructor<? extends Date> constructor = dateClassReplacement.getConstructor(Long.class);
					val = constructor.newInstance(dt.getTime());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				logger.warn("NPS param=[{}] has java.util.Date value, you can call setDateClassReplacement(<new class>)", index);
			}
		}
		preparedStatement.setObject(index, val);
	}

}
