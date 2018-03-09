package org.devocative.adroit.sql.ei;

import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExportImportHelper {
	private static final Logger logger = LoggerFactory.getLogger(ExportImportHelper.class);

	// ------------------------------

	private final Connection connection;
	private final Map<String, List<? extends Map<String, Object>>> dataSets = new LinkedHashMap<>();
	private final Map<String, Object> commonData = new HashMap<>();

	// ------------------------------

	public ExportImportHelper(Connection connection) {
		this.connection = connection;
	}

	// ------------------------------

	public Map<String, Object> getCommonData() {
		return commonData;
	}

	public ExportImportHelper setCommonData(Map<String, Object> commonData) {
		this.commonData.putAll(commonData);
		return this;
	}

	// ---------------

	public void exportBySql(String name, List<? extends Map<String, Object>> rows) {
		dataSets.put(name, rows);
		logger.info("Exported: name=[{}] count=[{}]", name, rows.size());
	}

	public Importer createImporter(String tableName, List<String> insertColumns, List<String> updateColumns, List<String> columnsOfUpdateCondition) {
		logger.info("Creating Importer: table=[{}]", tableName);

		return new Importer(connection, tableName, insertColumns, updateColumns, columnsOfUpdateCondition);
	}

	public Importer createImporter(String tableName, List<String> insertColumns) {
		logger.info("Creating Importer: table=[{}]", tableName);

		return new Importer(connection, tableName, insertColumns);
	}

	public Map<String, List<? extends Map<String, Object>>> getDataSets() {
		return new LinkedHashMap<>(dataSets);
	}

	public void merge(String dataKey, String idCol, String verCol, Map<Object, Object> currentData, Importer... imports) throws SQLException {
		if (dataSets.containsKey(dataKey)) {
			for (Map<String, Object> row : dataSets.get(dataKey)) {
				Object id = row.get(idCol);
				Comparable<Object> ver = (Comparable) row.get(verCol);

				if (currentData.containsKey(id)) {
					Object currentVer = currentData.get(id);
					if (ver.compareTo(currentVer) > 0) {
						for (Importer importer : imports) {
							importer.addUpdate(row, commonData);
						}
					}
				} else {
					for (Importer importer : imports) {
						importer.addInsert(row, commonData);
					}
				}
			}

			for (Importer importer : imports) {
				importer.executeBatch();
			}
		}
	}

	// ---------------

	public void exportTo(OutputStream stream) {
		XStream xStream = createXStream();
		xStream.toXML(dataSets, stream);
		logger.info("Exported to Stream: size=[{}] keys={}", dataSets.size(), dataSets.keySet());
	}

	public void importFrom(InputStream stream) {
		XStream xStream = createXStream();

		dataSets.putAll((LinkedHashMap) xStream.fromXML(stream));

		logger.info("Imported from Stream: size=[{}] keys={}", dataSets.size(), dataSets.keySet());
	}

	// ------------------------------

	protected XStream createXStream() {
		XStream xStream = new XStream();
		xStream.autodetectAnnotations(true);
		return xStream;
	}
}
