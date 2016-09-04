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

	/*
	Pattern to find parameters in the sql;
		(['].*?[']) try to ignore characters between two quote
		[:]([\w\d_]+) try to find parameter without :
	*/
	private static final String PARAM_PATTERN = "(['].*?['])|[:]([\\w\\d_]+)";

	// ------------------------------

	private Map<String, List<Integer>> paramsPlacement = new HashMap<>();
	private Map<String, Object> params = new HashMap<>();

	private boolean hasBatch = false;
	private boolean ignoreExtraPassedParam = false;
	private Class<? extends Date> dateClassReplacement;
	private String query, finalQuery, schema, id;

	private Connection connection;
	private PreparedStatement preparedStatement;

	private int batchSizeToFlush = 10000;
	private long totalBatchCount = 0, batchCount = 0;
	private Integer fetchSize, queryTimeout, maxRows;

	private Long pageIndex, pageSize;
	private EDatabaseType databaseType;

	// ------------------------------ CONSTRUCTORS

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

	// ------------------------------ UTIL METHODS

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

	public static List<String> findParamsInQuery(String query, boolean changeToLower) {
		List<String> result = new ArrayList<>();

		Pattern p = Pattern.compile(PARAM_PATTERN);
		Matcher matcher = p.matcher(query);

		while (matcher.find()) {
			if (matcher.group(1) == null) {
				String param;
				if (changeToLower) {
					param = matcher.group(2).toLowerCase();
				} else {
					param = matcher.group(2);
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

	public NamedParameterStatement setPageIndex(Long pageIndex) {
		this.pageIndex = pageIndex;
		return this;
	}

	public NamedParameterStatement setPageSize(Long pageSize) {
		this.pageSize = pageSize;
		return this;
	}

	public NamedParameterStatement setDatabaseType(EDatabaseType databaseType) {
		this.databaseType = databaseType;
		return this;
	}

	public NamedParameterStatement setIgnoreExtraPassedParam(boolean ignoreExtraPassedParam) {
		this.ignoreExtraPassedParam = ignoreExtraPassedParam;
		return this;
	}

	// ------------------------------ PUBLIC METHODS

	public ResultSet executeQuery() throws SQLException {
		applyPagination();
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

		Pattern p = Pattern.compile(PARAM_PATTERN);
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

		if (schema != null && schema.length() > 0) {
			finalQuery = applySchema(schema, finalQuery);
		}

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
	}

	private void applyPagination() {
		if (preparedStatement != null || (pageIndex == null && pageSize == null)) {
			return;
		}

		if (query == null) {
			throw new RuntimeException("Invalid NamedParameterStatement: no query!");
		}

		switch (findDatabaseType()) {

			case Oracle: // oracle records index starts from 1
				query = String.format(
					"select * from (select a.*, rownum rnum_pg from ( %s ) a) where rnum_pg between :pg_first and :pg_last",
					query);
				params.put("pg_first", (pageIndex - 1) * pageSize + 1);
				params.put("pg_last", pageIndex * pageSize);
				break;

			case MySql: // mysql records index starts from 0
				query = String.format("%s limit :pg_first,:pg_size", query);
				params.put("pg_first", (pageIndex - 1) * pageSize);
				params.put("pg_size", pageSize);
				break;

			case Unknown:
				logger.error("Unknown database type for pagination");
				break;
		}
	}

	private EDatabaseType findDatabaseType() {
		if (databaseType != null) {
			return databaseType;
		}

		EDatabaseType result = EDatabaseType.Unknown;
		try {
			String driverName = connection.getMetaData().getDriverName().toLowerCase();
			String url = connection.getMetaData().getURL().toLowerCase();
			for (EDatabaseType type : EDatabaseType.values()) {
				if (driverName.contains(type.getDriverHint()) || url.contains(type.getUrlHint())) {
					result = type;
					break;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
}
