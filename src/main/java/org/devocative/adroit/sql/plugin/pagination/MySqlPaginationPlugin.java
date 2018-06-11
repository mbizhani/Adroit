package org.devocative.adroit.sql.plugin.pagination;

import org.devocative.adroit.sql.plugin.PaginationPlugin;

import java.util.Map;

public class MySqlPaginationPlugin extends PaginationPlugin {
	public MySqlPaginationPlugin(Long firstResult, Long maxResults) {
		super(firstResult, maxResults);
	}

	@Override
	public String process(String query, Map<String, Object> params) {
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

		return query;
	}
}
