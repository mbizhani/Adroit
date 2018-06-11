package org.devocative.adroit.sql.info;

import org.devocative.adroit.sql.plugin.PaginationPlugin;
import org.devocative.adroit.sql.plugin.pagination.MySqlPaginationPlugin;

public class MySqlDatabaseInfo implements IDatabaseInfo {
	@Override
	public String getName() {
		return "MySql";
	}

	@Override
	public String getDriverHint() {
		return "mysql";
	}

	@Override
	public String getUrlHint() {
		return "jdbc:mysql";
	}

	@Override
	public PaginationPlugin createPagination(Long firstResult, Long maxResults) {
		return new MySqlPaginationPlugin(firstResult, maxResults);
	}
}
