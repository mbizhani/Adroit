package org.devocative.adroit.sql.plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class PaginationPlugin implements INpsPlugin {
	private Long firstResult, maxResults;
	private EDatabaseType databaseType;

	// ------------------------------

	public PaginationPlugin(Long firstResult, Long maxResults, EDatabaseType databaseType) {
		this.firstResult = firstResult;
		this.maxResults = maxResults;
		this.databaseType = databaseType;
	}

	// ------------------------------

	@Override
	public String process(String query, Map<String, Object> params) {
		if (firstResult != null && firstResult < 1) {
			throw new RuntimeException("Invalid 'firstResult' value, must be greater than zero: " + firstResult);
		}

		if (maxResults != null && maxResults < 1) {
			throw new RuntimeException("Invalid 'maxResults' value, must be greater than zero: " + maxResults);
		}

		switch (databaseType) {

			case Oracle: // oracle records index starts from 1
			case HSQLDB: // SET DATABASE SQL SYNTAX ORA TRUE
				if (firstResult != null && maxResults != null) {
					query = String.format(
						"select * from (select a.*, rownum rnum_pg from ( %s ) a) where rnum_pg between :pg_first and :pg_last",
						query);
					params.put("pg_first", firstResult);
					params.put("pg_last", firstResult + maxResults - 1);
				} else if (firstResult != null) {
					query = String.format(
						"select * from (select a.*, rownum rnum_pg from ( %s ) a) where rnum_pg >= :pg_first",
						query);
					params.put("pg_first", firstResult);
				} else {
					query = String.format(
						"select * from (select a.*, rownum rnum_pg from ( %s ) a) where rnum_pg <= :pg_last",
						query);
					params.put("pg_last", maxResults);
				}
				break;

			case MySql: // mysql records index starts from 0
				if (firstResult != null && maxResults != null) {
					query = String.format("%s limit :pg_first,:pg_size", query);
					params.put("pg_first", firstResult - 1);
					params.put("pg_size", maxResults);
				} else if (firstResult != null) {
					query = String.format("%s limit :pg_first,:pg_size", query);
					params.put("pg_first", firstResult - 1);
					params.put("pg_size", Long.MAX_VALUE);
				} else {
					query = String.format("%s limit :pg_size", query);
					params.put("pg_size", maxResults);
				}
				break;
		}

		return query;
	}

	// ------------------------------

	public static EDatabaseType findDatabaseType(Connection connection) {
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
