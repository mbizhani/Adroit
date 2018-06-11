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
				add(entry.getKey(), FilterValue.contain(String.format("%%%s%%", entry.getValue())));
			} else {
				add(entry.getKey(), FilterValue.equal(entry.getValue()));
			}
		}
		return this;
	}

	@Override
	public String process(String query, Map<String, Object> params) {
		if (!filter.isEmpty()) {
			StringBuilder filterBuilder = new StringBuilder();
			for (Map.Entry<String, FilterValue> entry : filter.entrySet()) {
				final String filter = entry.getKey();
				final String filterParam = filter.replaceAll("[.]", "__");

				final FilterValue filterValue = entry.getValue();
				final String sqlFunc = filterValue.getSqlFunc();

				switch (filterValue.getType()) {
					case Equal:
						if (filterValue.getValue() instanceof Collection) {
							//TODO can't apply filter's sql function here!
							filterBuilder.append(String.format("\tand %s in (:%s)\n", filter, filterParam));
						} else {
							if (sqlFunc == null) {
								filterBuilder.append(String.format("\tand %s = :%s\n", filter, filterParam));
							} else {
								filterBuilder.append(String.format("\tand %1$s(%2$s) = %1$s(:%3$s)\n", sqlFunc, filter, filterParam));
							}
						}
						params.put(filterParam, filterValue.getValue());
						break;

					case Range:
					case Between:
						if (filterValue.getLower() != null) {
							if (sqlFunc == null) {
								filterBuilder.append(String.format("\tand %s >= :%s_l\n", filter, filterParam));
							} else {
								filterBuilder.append(String.format("\tand %1$s(%2$s) >= %1$s(:%3$s_l)\n", sqlFunc, filter, filterParam));
							}
							params.put(filterParam + "_l", filterValue.getLower());
						}

						if (filterValue.getUpper() != null) {
							String opr = filterValue.getType() == FilterType.Between ? "<=" : "<";
							if (sqlFunc == null) {
								filterBuilder.append(String.format("\tand %s %s :%s_u\n", filter, opr, filterParam));
							} else {
								filterBuilder.append(String.format("\tand %1$s(%2$s) %3$s %1$s(:%4$s_u)\n", sqlFunc, filter, opr, filterParam));
							}
							params.put(filterParam + "_u", filterValue.getUpper());
						}
						break;

					case Contain:
						if (sqlFunc == null) {
							filterBuilder.append(String.format("\tand %s like :%s\n", filter, filterParam));
						} else {
							filterBuilder.append(String.format("\tand %1$s(%2$s) like %1$s(:%3$s)\n", sqlFunc, filter, filterParam));
						}
						params.put(filterParam, filterValue.getValue());
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
