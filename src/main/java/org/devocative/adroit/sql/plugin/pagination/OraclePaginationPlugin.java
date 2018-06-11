package org.devocative.adroit.sql.plugin.pagination;

import org.devocative.adroit.sql.plugin.PaginationPlugin;

import java.util.Map;

public class OraclePaginationPlugin extends PaginationPlugin {

	public OraclePaginationPlugin(Long firstResult, Long maxResults) {
		super(firstResult, maxResults);
	}

	@Override
	public String process(String query, Map<String, Object> params) {
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

		return query;
	}
}
