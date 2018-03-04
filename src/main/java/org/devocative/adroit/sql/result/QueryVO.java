package org.devocative.adroit.sql.result;

import org.devocative.adroit.ObjectUtil;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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

	public List<List<Object>> getRows() {
		return rows;
	}

	// ---------------

	public List<RowVO> toListOfMap() {
		List<RowVO> list = new ArrayList<>();
		for (List<Object> row : rows) {
			RowVO rowAsMap = new RowVO();
			for (int i = 0; i < header.size(); i++) {
				rowAsMap.put(header.get(i), row.get(i));
			}
			list.add(rowAsMap);
		}

		return list;
	}

	public <T> List<T> toBeans(Class<T> cls) {
		try {
			Map<String, String> properties = new HashMap<>();
			PropertyDescriptor[] propertyDescriptors = ObjectUtil.getPropertyDescriptors(cls, false);
			for (PropertyDescriptor prop : propertyDescriptors) {
				if (prop.getWriteMethod() != null) {
					properties.put(prop.getName().toLowerCase(), prop.getName());
				}
			}

			List<T> result = new ArrayList<>();

			List<RowVO> rows = toListOfMap();
			for (RowVO row : rows) {
				T bean = cls.newInstance();

				for (Map.Entry<String, Object> cell : row.entrySet()) {
					String propGuess = cell.getKey().toLowerCase().replaceAll("[_]", "");
					if (properties.containsKey(propGuess)) {
						String prop = properties.get(propGuess);
						ObjectUtil.setPropertyValue(bean, prop, cell.getValue(), true);
					}
				}

				result.add(bean);
			}

			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ------------------------------

	void addHeader(String header) {
		this.header.add(header);
	}

	void addRow(List<Object> row) {
		rows.add(row);
	}
}
