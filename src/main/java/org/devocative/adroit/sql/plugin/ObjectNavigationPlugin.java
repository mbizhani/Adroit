package org.devocative.adroit.sql.plugin;

import org.devocative.adroit.ObjectUtil;
import org.devocative.adroit.sql.NamedParameterStatement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ObjectNavigationPlugin implements INpsPlugin {
	private String specialParamPrefix = "$$";
	private String propertyNavSplitChar = "$";
	private String propertyNavSplitCharRegex = "\\$";

	// ------------------------------

	public ObjectNavigationPlugin setSpecialParamPrefix(String specialParamPrefix) {
		this.specialParamPrefix = specialParamPrefix;
		return this;
	}

	public ObjectNavigationPlugin setPropertyNavSplitChar(String propertyNavSplitChar) {
		this.propertyNavSplitChar = propertyNavSplitChar;
		return this;
	}

	public ObjectNavigationPlugin setPropertyNavSplitCharRegex(String propertyNavSplitCharRegex) {
		this.propertyNavSplitCharRegex = propertyNavSplitCharRegex;
		return this;
	}

	// ---------------

	@Override
	public String process(String query, Map<String, Object> params) {
		Map<String, Object> temp = new LinkedHashMap<>();
		List<String> paramsInQuery = NamedParameterStatement.findParamsInQuery(query, false);

		for (Map.Entry<String, Object> entry : params.entrySet()) {
			if (entry.getKey().startsWith(specialParamPrefix)) {
				String sentParam = entry.getKey() + propertyNavSplitChar;
				for (String paramInQuery : paramsInQuery) {
					if (paramInQuery.startsWith(sentParam)) {
						String[] props = paramInQuery
							.substring(sentParam.length() - 1)
							.split(propertyNavSplitCharRegex);
						Object value = entry.getValue();
						for (String prop : props) {
							if (prop.trim().length() > 0) {
								value = ObjectUtil.getPropertyValue(value, prop, false);
							}
						}
						temp.put(paramInQuery, value);
					}
				}
			}
		}

		params.putAll(temp);

		return query;
	}
}
