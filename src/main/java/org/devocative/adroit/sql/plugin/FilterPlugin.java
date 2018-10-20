package org.devocative.adroit.sql.plugin;

import org.devocative.adroit.sql.filter.FilterType;
import org.devocative.adroit.sql.filter.FilterValue;
import org.devocative.adroit.sql.sort.SortValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FilterPlugin implements INpsPlugin {
	public static final String EMBED_FILTER_EXPRESSION = "%FILTER%";

	private final String selectFields;
	private final List<FilterValue> filters;
	private final List<SortValue> sorts;

	// ------------------------------

	public FilterPlugin() {
		this(new ArrayList<>());
	}

	public FilterPlugin(List<FilterValue> filters) {
		this("*", filters, new ArrayList<>());
	}

	public FilterPlugin(String selectFields, List<FilterValue> filters, List<SortValue> sorts) {
		this.selectFields = selectFields;
		this.filters = filters;
		this.sorts = sorts;
	}

	// ------------------------------

	public FilterPlugin add(FilterValue filterValue) {
		filters.add(filterValue);
		return this;
	}

	public FilterPlugin autoAdd(Map<String, Object> filter) {
		for (Map.Entry<String, Object> entry : filter.entrySet()) {
			if (entry.getValue() instanceof String) {
				add(FilterValue.contain(entry.getKey(), String.format("%%%s%%", entry.getValue())));
			} else {
				add(FilterValue.equal(entry.getKey(), entry.getValue()));
			}
		}
		return this;
	}

	@Override
	public String process(String query, Map<String, Object> params) {
		if (!filters.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			for (FilterValue filterValue : filters) {
				final String filter = filterValue.getField();
				final String filterParam = filter.replaceAll("[.]", "__");

				final String sqlFunc = filterValue.getSqlFunc();

				switch (filterValue.getType()) {
					case Equal:
						if (filterValue.getValue() instanceof Collection) {
							//TODO can't apply filter's sql function here!
							builder.append(String.format("\tand %s in (:%s)\n", filter, filterParam));
						} else {
							if (sqlFunc == null) {
								builder.append(String.format("\tand %s = :%s\n", filter, filterParam));
							} else {
								builder.append(String.format("\tand %1$s(%2$s) = %1$s(:%3$s)\n", sqlFunc, filter, filterParam));
							}
						}
						params.put(filterParam, filterValue.getValue());
						break;

					case Range:
					case Between:
						if (filterValue.getLower() != null) {
							if (sqlFunc == null) {
								builder.append(String.format("\tand %s >= :%s_l\n", filter, filterParam));
							} else {
								builder.append(String.format("\tand %1$s(%2$s) >= %1$s(:%3$s_l)\n", sqlFunc, filter, filterParam));
							}
							params.put(filterParam + "_l", filterValue.getLower());
						}

						if (filterValue.getUpper() != null) {
							String opr = filterValue.getType() == FilterType.Between ? "<=" : "<";
							if (sqlFunc == null) {
								builder.append(String.format("\tand %s %s :%s_u\n", filter, opr, filterParam));
							} else {
								builder.append(String.format("\tand %1$s(%2$s) %3$s %1$s(:%4$s_u)\n", sqlFunc, filter, opr, filterParam));
							}
							params.put(filterParam + "_u", filterValue.getUpper());
						}
						break;

					case Contain:
						if (sqlFunc == null) {
							builder.append(String.format("\tand %s like :%s\n", filter, filterParam));
						} else {
							builder.append(String.format("\tand %1$s(%2$s) like %1$s(:%3$s)\n", sqlFunc, filter, filterParam));
						}
						params.put(filterParam, filterValue.getValue());
						break;

					default:
						throw new RuntimeException(String.format("Invalid Filter Type: type=[%s] key=[%s] value=[%s]",
							filterValue.getType(), filter, filterValue.getValue()));
				}
			}

			if (!sorts.isEmpty()) {
				builder
					.append(" order by ")
					.append(sorts.stream()
						.map(s -> String.format("%s %s", s.getField(), s.getType()))
						.collect(Collectors.joining(","))
					);
			}

			if (query.contains(EMBED_FILTER_EXPRESSION)) {
				return query.replace(EMBED_FILTER_EXPRESSION, builder.toString());
			}

			return String.format("select %s from ( %s ) where 1=1\n%s", selectFields, query, builder.toString());
		} else if (query.contains(EMBED_FILTER_EXPRESSION)) {
			return query.replace(EMBED_FILTER_EXPRESSION, "");
		}

		return query;
	}
}
