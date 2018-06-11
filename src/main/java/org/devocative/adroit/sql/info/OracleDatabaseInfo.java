package org.devocative.adroit.sql.info;

import org.devocative.adroit.sql.plugin.PaginationPlugin;
import org.devocative.adroit.sql.plugin.pagination.OraclePaginationPlugin;

public class OracleDatabaseInfo implements IDatabaseInfo {
	@Override
	public String getName() {
		return "Oracle";
	}

	@Override
	public String getDriverHint() {
		return "oracledriver";
	}

	@Override
	public String getUrlHint() {
		return "jdbc:oracle";
	}

	@Override
	public PaginationPlugin createPagination(Long firstResult, Long maxResults) {
		return new OraclePaginationPlugin(firstResult, maxResults);
	}
}
