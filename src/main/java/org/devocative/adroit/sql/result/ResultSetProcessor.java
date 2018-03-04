package org.devocative.adroit.sql.result;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResultSetProcessor {

	public static void processRowAsList(ResultSet rs, IRowAsList asList) throws SQLException {
		processRowAsList(rs, rs.getMetaData(), asList);
	}

	public static void processRowAsList(ResultSet rs, ResultSetMetaData metaData, IRowAsList asList) throws SQLException {
		int size = metaData.getColumnCount();

		while (rs.next()) {
			List<Object> row = new ArrayList<>();
			for (int i = 1; i <= size; i++) {
				row.add(getValue(rs, i, metaData.getColumnType(i)));
			}
			asList.onRowResult(row);
		}
	}

	public static void processRowAsMap(ResultSet rs, IRowAsMap asMap, EColumnNameCase nameCase) throws SQLException {
		processRowAsMap(rs, rs.getMetaData(), asMap, nameCase);
	}

	public static void processRowAsMap(ResultSet rs, ResultSetMetaData metaData, IRowAsMap asMap, EColumnNameCase nameCase) throws SQLException {
		int size = metaData.getColumnCount();

		while (rs.next()) {
			Map<String, Object> row = new LinkedHashMap<>();
			for (int i = 1; i <= size; i++) {
				String colName = metaData.getColumnName(i);
				switch (nameCase) {
					case LOWER:
						colName = colName.toLowerCase();
						break;
					case UPPER:
						colName = colName.toUpperCase();
						break;
				}
				row.put(colName, getValue(rs, i, metaData.getColumnType(i)));
			}
			asMap.onRowResult(row);
		}
	}

	public static QueryVO process(ResultSet rs, EColumnNameCase nameCase) throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();
		QueryVO result = new QueryVO();

		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			String colName = metaData.getColumnName(i);
			switch (nameCase) {
				case LOWER:
					colName = colName.toLowerCase();
					break;
				case UPPER:
					colName = colName.toUpperCase();
					break;
			}
			result.addHeader(colName);
		}

		processRowAsList(rs, metaData, result::addRow);

		return result;
	}

	public static Object getValue(ResultSet rs, int colIndex, int colType) throws SQLException {
		Object value;
		switch (colType) {
			case Types.DATE:
				value = rs.getDate(colIndex);
				break;
			case Types.TIME:
				value = rs.getTime(colIndex);
				break;
			case Types.TIMESTAMP:
				value = rs.getTimestamp(colIndex);
				break;
			case Types.CLOB:
				value = rs.getString(colIndex);
				break;
			default:
				value = rs.getObject(colIndex);
		}
		return value;
	}
}
