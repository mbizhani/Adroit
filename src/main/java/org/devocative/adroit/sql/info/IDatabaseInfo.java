package org.devocative.adroit.sql.info;

import org.devocative.adroit.sql.plugin.PaginationPlugin;

public interface IDatabaseInfo {
	String getName();

	String getDriverHint();

	String getUrlHint();

	PaginationPlugin createPagination(Long firstResult, Long maxResults);
}
