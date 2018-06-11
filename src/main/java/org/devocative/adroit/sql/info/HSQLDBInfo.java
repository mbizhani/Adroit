package org.devocative.adroit.sql.info;

import org.devocative.adroit.sql.plugin.PaginationPlugin;
import org.devocative.adroit.sql.plugin.pagination.OraclePaginationPlugin;

public class HSQLDBInfo implements IDatabaseInfo {
	@Override
	public String getName() {
		return "HSQLDB";
	}

	@Override
	public String getDriverHint() {
		return "hsqldb";
	}

	@Override
	public String getUrlHint() {
		return "jdbc:hsqldb";
	}

	@Override
	public PaginationPlugin createPagination(Long firstResult, Long maxResults) {
		return new OraclePaginationPlugin(firstResult, maxResults);
	}
}
