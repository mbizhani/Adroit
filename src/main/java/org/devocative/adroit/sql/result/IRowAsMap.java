package org.devocative.adroit.sql.result;

import java.util.Map;

public interface IRowAsMap {
	void onRowResult(Map<String, Object> row);
}
