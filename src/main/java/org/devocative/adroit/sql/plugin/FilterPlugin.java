package org.devocative.adroit.sql.plugin;

import org.devocative.adroit.sql.filter.FilterType;
import org.devocative.adroit.sql.filter.FilterValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FilterPlugin implements INpsPlugin {
	public static final String EMBED_FILTER_EXPRESSION = "%FILTER%";

	private Map<String, FilterValue> filter;

	// ------------------------------

	public FilterPlugin() {
		this(new HashMap<>());
	}

	public FilterPlugin(Map<String, FilterValue> filter) {
		this.filter = filter;
	}

	// ------------------------------

	public FilterPlugin add(String filter, FilterValue filterValue) {
		this.filter.put(filter, filterValue);
		return this;
	}

	public FilterPlugin addAll(Map<String, Object> filter) {
		for (Map.Entry<String, Object> entry : filter.entrySet()) {
			if (entry.getValue() instanceof String) {
				add(entry.getKey(), new FilterValue(String.format("%%%s%%", entry.getValue()), FilterType.ContainNoCase));
			} else {
				add(entry.getKey(), new FilterValue(entry.getValue(), FilterType.Equal));
			}
		}
		return this;
	}

	@Override
	public String process(String query, Map<String, Object> params) {
		if (!filter.isEmpty()) {
			StringBuilder filterBuilder = new StringBuilder();
			for (Map.Entry<String, FilterValue> entry : filter.entrySet()) {
				String filter = entry.getKey();
				FilterValue filterValue = entry.getValue();

				switch (filterValue.getType()) {
					case Equal:
						if (filterValue.getValue() instanceof Collection) {
							filterBuilder.append(String.format("\tand %1$s in (:%1$s)\n", filter));
						} else {
							filterBuilder.append(String.format("\tand %1$s = :%1$s\n", filter));
						}
						params.put(filter, filterValue.getValue());
						break;

					case Range:
					case Between:
						if (filterValue.getValue() != null) {
							filterBuilder.append(String.format("\tand %1$s >= :%1$s_l\n", filter));
							params.put(filter + "_l", filterValue.getValue());
						}

						if (filterValue.getUpper() != null) {
							if (filterValue.getType() == FilterType.Between) {
								filterBuilder.append(String.format("\tand %1$s <= :%1$s_u\n", filter));
							} else {
								filterBuilder.append(String.format("\tand %1$s < :%1$s_u\n", filter));
							}
							params.put(filter + "_u", filterValue.getUpper());
						}
						break;

					case ContainCase:
						filterBuilder.append(String.format("\tand %1$s like :%1$s\n", filter));
						params.put(filter, filterValue.getValue());
						break;

					case ContainNoCase:
						filterBuilder.append(String.format("\tand lower(%1$s) like lower(:%1$s)\n", filter));
						params.put(filter, filterValue.getValue());
						break;

					default:
						throw new RuntimeException(String.format("Invalid Filter Type: type=[%s] key=[%s] value=[%s]",
							filterValue.getType(), filter, filterValue.getValue()));
				}
			}

			if (query.contains(EMBED_FILTER_EXPRESSION)) {
				return query.replace(EMBED_FILTER_EXPRESSION, filterBuilder.toString());
			}

			return String.format("select * from ( %s ) where 1=1\n%s", query, filterBuilder.toString());
		} else if (query.contains(EMBED_FILTER_EXPRESSION)) {
			return query.replace(EMBED_FILTER_EXPRESSION, "");
		}

		return query;
	}
}
