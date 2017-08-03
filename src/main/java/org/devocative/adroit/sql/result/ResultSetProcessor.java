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
		ResultSetMetaData metaData = rs.getMetaData();
		int size = metaData.getColumnCount();

		while (rs.next()) {
			List<Object> row = new ArrayList<>();
			for (int i = 1; i <= size; i++) {
				row.add(getValue(rs, metaData, i));
			}
			asList.onRowResult(row);
		}
	}

	public static void processRowAsMap(ResultSet rs, IRowAsMap asMap, EColumnNameCase nameCase) throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();
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
				row.put(colName, getValue(rs, metaData, i));
			}
			asMap.onRowResult(row);
		}
	}

	public static Object getValue(ResultSet rs, ResultSetMetaData metaData, int colIndex) throws SQLException {
		Object value;
		switch (metaData.getColumnType(colIndex)) {
			case Types.DATE:
				value = rs.getDate(colIndex);
				break;
			case Types.TIME:
				value = rs.getTime(colIndex);
				break;
			case Types.TIMESTAMP:
				value = rs.getTimestamp(colIndex);
				break;
			default:
				value = rs.getObject(colIndex);
		}
		return value;
	}

}
