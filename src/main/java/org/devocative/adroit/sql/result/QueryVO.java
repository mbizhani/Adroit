package org.devocative.adroit.sql.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueryVO implements Serializable {
	private static final long serialVersionUID = 7622836172393602198L;

	private List<String> header = new ArrayList<>();
	private List<List<Object>> rows = new ArrayList<>();

	// ------------------------------

	public List<String> getHeader() {
		return header;
	}

	public void addHeader(String header) {
		this.header.add(header);
	}

	public void addRow(List<Object> row) {
		rows.add(row);
	}

	public List<List<Object>> getRows() {
		return rows;
	}

	// ---------------

	public List<Map<String, Object>> toListOfMap() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (List<Object> row : rows) {
			Map<String, Object> rowAsMap = new LinkedHashMap<>();
			for (int i = 0; i < header.size(); i++) {
				rowAsMap.put(header.get(i), row.get(i));
			}
			list.add(rowAsMap);
		}

		return list;
	}
}
