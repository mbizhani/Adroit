package org.devocative.adroit.sql.plugin;

import org.devocative.adroit.ObjectUtil;
import org.devocative.adroit.sql.NamedParameterStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ObjectNavigationPlugin implements INpsPlugin {
	private static final Logger logger = LoggerFactory.getLogger(ObjectNavigationPlugin.class);

	private String specialParamPrefix = "$$";
	private String propertyNavSplitChar = "$";
	private String propertyNavSplitCharRegex = "\\$";
	private boolean innerCollectionNavigation = true;
	private boolean forceUnknownProperty = false;

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

	public ObjectNavigationPlugin setInnerCollectionNavigation(boolean innerCollectionNavigation) {
		this.innerCollectionNavigation = innerCollectionNavigation;
		return this;
	}

	public ObjectNavigationPlugin setForceUnknownProperty(boolean forceUnknownProperty) {
		this.forceUnknownProperty = forceUnknownProperty;
		return this;
	}

	// ---------------

	@Override
	public String process(String query, Map<String, Object> params) {
		Map<String, Object> temp = new HashMap<>();
		List<String> paramsInQuery = NamedParameterStatement.findParamsInQuery(query, false);

		for (Map.Entry<String, Object> entry : params.entrySet()) {
			if (entry.getKey().startsWith(specialParamPrefix)) {
				logger.debug("ObjectNavigationPlugin: param found [{}]", entry.getKey());

				String sentParam = entry.getKey() + propertyNavSplitChar;
				for (String paramInQuery : paramsInQuery) {
					if (paramInQuery.startsWith(sentParam)) {
						String[] props = paramInQuery
							.substring(sentParam.length() - 1)
							.split(propertyNavSplitCharRegex);

						Object value = entry.getValue();

						for (String prop : props) {
							if (value != null && prop.trim().length() > 0) {
								if (innerCollectionNavigation) {
									if (value instanceof Collection) {
										List<Object> list = new ArrayList<>();
										for (Object elem : (Collection) value) {
											list.add(ObjectUtil.getPropertyValue(elem, prop, forceUnknownProperty));
										}
										value = list;
									} else if (value.getClass().isArray()) {
										List<Object> list = new ArrayList<>();
										for (Object elem : (Object[]) value) {
											list.add(ObjectUtil.getPropertyValue(elem, prop, forceUnknownProperty));
										}
										value = list;
									} else {
										value = ObjectUtil.getPropertyValue(value, prop, forceUnknownProperty);
									}
								} else {
									value = ObjectUtil.getPropertyValue(value, prop, forceUnknownProperty);
								}
							}
						}
						temp.put(paramInQuery, value);
					}
				}
			}
		}

		logger.debug("ObjectNavigationPlugin: set params [{}]", temp);

		params.putAll(temp);

		return query;
	}
}
