package org.devocative.adroit.sql;

import org.devocative.adroit.sql.info.HSQLDBInfo;
import org.devocative.adroit.sql.info.IDatabaseInfo;
import org.devocative.adroit.sql.info.OracleDatabaseInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseType {
	private static final Map<String, IDatabaseInfo> INFO_MAP = new HashMap<>();

	static {
		register(new OracleDatabaseInfo());
		register(new HSQLDBInfo());
	}

	// ------------------------------

	public static void register(IDatabaseInfo databaseInfo) {
		INFO_MAP.put(databaseInfo.getName(), databaseInfo);
	}

	public static IDatabaseInfo find(Connection connection) {
		IDatabaseInfo result = null;

		String driverName = null;
		String url = null;

		try {
			driverName = connection.getMetaData().getDriverName().toLowerCase();
			url = connection.getMetaData().getURL().toLowerCase();
			for (IDatabaseInfo type : INFO_MAP.values()) {
				if (driverName.contains(type.getDriverHint()) || url.contains(type.getUrlHint())) {
					result = type;
					break;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(String.format("Find Database Type: driver=[%s] url=[%s]", driverName, url), e);
		}

		return result;
	}
}
