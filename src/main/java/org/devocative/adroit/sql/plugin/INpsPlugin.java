package org.devocative.adroit.sql.plugin;

import java.util.Map;

public interface INpsPlugin {
	String process(String query, Map<String, Object> params);
}
