package org.devocative.adroit.sql.plugin;

import org.devocative.adroit.sql.DatabaseType;

import java.sql.Connection;

public abstract class PaginationPlugin implements INpsPlugin {
	protected final Long firstResult;
	protected final Long maxResults;

	// ------------------------------

	protected PaginationPlugin(Long firstResult, Long maxResults) {
		this.firstResult = firstResult;
		this.maxResults = maxResults;
	}

	// ------------------------------

	public static PaginationPlugin of(Connection connection, Long firstResult, Long maxResults) {
		return DatabaseType.find(connection).createPagination(firstResult, maxResults);
	}
}
