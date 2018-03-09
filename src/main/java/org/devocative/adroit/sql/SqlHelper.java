package org.devocative.adroit.sql;

import com.thoughtworks.xstream.XStream;
import org.devocative.adroit.sql.plugin.INpsPlugin;
import org.devocative.adroit.sql.result.EColumnNameCase;
import org.devocative.adroit.sql.result.QueryVO;
import org.devocative.adroit.sql.result.ResultSetProcessor;
import org.devocative.adroit.xml.AdroitXStream;

import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class SqlHelper {
	private final Connection connection;
	private boolean ignoreExtraPassedParam = true;
	private EColumnNameCase nameCase = EColumnNameCase.LOWER;
	private Class<? extends Date> dateClassReplacement = java.sql.Date.class;

	private Map<String, XQuery> xQueryMap = new HashMap<>();

	// ------------------------------

	public SqlHelper(Connection connection) {
		this.connection = connection;
	}

	// ------------------------------

	public SqlHelper setIgnoreExtraPassedParam(boolean ignoreExtraPassedParam) {
		this.ignoreExtraPassedParam = ignoreExtraPassedParam;
		return this;
	}

	public SqlHelper setNameCase(EColumnNameCase nameCase) {
		this.nameCase = nameCase;
		return this;
	}

	public SqlHelper setDateClassReplacement(Class<? extends Date> dateClassReplacement) {
		this.dateClassReplacement = dateClassReplacement;
		return this;
	}

	public SqlHelper setXMLQueryFile(InputStream in) {
		XStream xStream = new AdroitXStream();
		xStream.processAnnotations(XQuery.class);

		List<XQuery> xQueries = (List<XQuery>) xStream.fromXML(in);
		for (XQuery xQuery : xQueries) {
			xQueryMap.put(xQuery.getName(), xQuery);
		}
		return this;
	}

	// ---------------

	public NamedParameterStatement createNPS(String name) {
		return createNPS(xQueryMap.get(name));
	}

	public NamedParameterStatement createNPS(XQuery sql) {
		return new NamedParameterStatement(connection, sql.getSql())
			.setIgnoreExtraPassedParam(ignoreExtraPassedParam)
			.setDateClassReplacement(dateClassReplacement);
	}

	// ---------------

	public <K, V> Map<K, V> twoCellsAsMap(String name) throws SQLException {
		return twoCellsAsMap(name, new HashMap<>());
	}

	public <K, V> Map<K, V> twoCellsAsMap(String name, Map<String, Object> params, INpsPlugin... plugins) throws SQLException {
		return twoCellsAsMap(xQueryMap.get(name), params, plugins);
	}

	public <K, V> Map<K, V> twoCellsAsMap(XQuery sql) throws SQLException {
		return twoCellsAsMap(sql, new HashMap<>());
	}

	public <K, V> Map<K, V> twoCellsAsMap(XQuery sql, Map<String, Object> params, INpsPlugin... plugins) throws SQLException {
		NamedParameterStatement nps = createNPS(sql, params, plugins);

		ResultSet rs = nps.executeQuery();
		ResultSetMetaData metaData = rs.getMetaData();
		int col1Type = metaData.getColumnType(1);
		int col2Type = metaData.getColumnType(2);

		Map<K, V> result = new LinkedHashMap<>();
		while (rs.next()) {
			K key = (K) ResultSetProcessor.getValue(rs, 1, col1Type);
			V value = (V) ResultSetProcessor.getValue(rs, 2, col2Type);
			result.put(key, value);
		}

		nps.close();

		return result;
	}

	// -----

	public List<Object> firstRowAsList(String name) throws SQLException {
		return firstRowAsList(name, new HashMap<>());
	}

	public List<Object> firstRowAsList(String name, Map<String, Object> params, INpsPlugin... plugins) throws SQLException {
		return firstRowAsList(xQueryMap.get(name), params, plugins);
	}

	public List<Object> firstRowAsList(XQuery sql) throws SQLException {
		return firstRowAsList(sql, new HashMap<>());
	}

	public List<Object> firstRowAsList(XQuery sql, Map<String, Object> params, INpsPlugin... plugins) throws SQLException {
		NamedParameterStatement nps = createNPS(sql, params, plugins);

		ResultSet rs = nps.executeQuery();
		int columnCount = rs.getMetaData().getColumnCount();

		List<Object> result = new ArrayList<>();
		if (rs.next()) {
			for (int i = 1; i <= columnCount; i++) {
				result.add(rs.getObject(i));
			}
		}
		nps.close();
		return result;
	}

	// -----

	public <T> List<T> firstColAsList(String name) throws SQLException {
		return firstColAsList(name, new HashMap<>());
	}

	public <T> List<T> firstColAsList(String name, Map<String, Object> params, INpsPlugin... plugins) throws SQLException {
		return firstColAsList(xQueryMap.get(name), params, plugins);
	}

	public <T> List<T> firstColAsList(XQuery sql) throws SQLException {
		return firstColAsList(sql, new HashMap<>());
	}

	public <T> List<T> firstColAsList(XQuery sql, Map<String, Object> params, INpsPlugin... plugins) throws SQLException {
		NamedParameterStatement nps = createNPS(sql, params, plugins);

		ResultSet rs = nps.executeQuery();
		int col1Type = rs.getMetaData().getColumnType(1);

		List<T> result = new ArrayList<>();
		while (rs.next()) {
			result.add((T) ResultSetProcessor.getValue(rs, 1, col1Type));
		}
		nps.close();
		return result;
	}

	// -----

	public Object firstCell(String name) throws SQLException {
		return firstCell(name, new HashMap<>());
	}

	public Object firstCell(String name, Map<String, Object> params, INpsPlugin... plugins) throws SQLException {
		return firstCell(xQueryMap.get(name), params, plugins);
	}

	public Object firstCell(XQuery sql) throws SQLException {
		return firstCell(sql, new HashMap<>());
	}

	public Object firstCell(XQuery sql, Map<String, Object> params, INpsPlugin... plugins) throws SQLException {
		NamedParameterStatement nps = createNPS(sql, params, plugins);

		ResultSet rs = nps.executeQuery();
		ResultSetMetaData metaData = rs.getMetaData();

		Object result = null;
		if (rs.next()) {
			result = ResultSetProcessor.getValue(rs, 1, metaData.getColumnType(1));
		}

		nps.close();

		return result;
	}

	// -----

	public QueryVO selectAll(String name) throws SQLException {
		return selectAll(name, new HashMap<>());
	}

	public QueryVO selectAll(String name, Map<String, Object> params, INpsPlugin... plugins) throws SQLException {
		return selectAll(xQueryMap.get(name), params, plugins);
	}

	public QueryVO selectAll(XQuery sql) throws SQLException {
		return selectAll(sql, new HashMap<>());
	}

	public QueryVO selectAll(XQuery sql, Map<String, Object> params, INpsPlugin... plugins) throws SQLException {
		NamedParameterStatement nps = createNPS(sql, params, plugins);
		ResultSet resultSet = nps.executeQuery();
		QueryVO queryVO = ResultSetProcessor.process(resultSet, nameCase);
		nps.close();
		return queryVO;
	}

	// -----

	public void executeDDL(String name, Map<String, Object> params) throws SQLException {
		String ddl = xQueryMap.get(name).getSql();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			ddl = ddl.replaceAll("[:]" + entry.getKey(), entry.getValue().toString());
		}

		Statement st = connection.createStatement();
		st.executeUpdate(ddl);
		st.close();
	}

	// ------------------------------

	private NamedParameterStatement createNPS(XQuery sql, Map<String, Object> params, INpsPlugin... plugins) {
		NamedParameterStatement nps = createNPS(sql)
			.setParameters(params);

		if (plugins != null) {
			for (INpsPlugin plugin : plugins) {
				if (plugin != null) {
					nps.addPlugin(plugin);
				}
			}
		}

		return nps;
	}
}