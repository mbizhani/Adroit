package org.devocative.adroit.sql.plugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchemaPlugin implements INpsPlugin {
	private static final Pattern SCHEMA_PATTERN = Pattern.compile(
		"(['].*?['])|([\"].*?[\"])|(--.*?\\n)|(/[*].*?[*]/)|(extract[(].+?[)])|(from|join|into|update)[\\s]+(\\w+([.]\\w+)?)",
		Pattern.CASE_INSENSITIVE);

	private static final Set<String> KEYWORDS = new HashSet<>();

	static {
		KEYWORDS.add("set");
		KEYWORDS.add("dual");
	}

	// ------------------------------

	private String schema;

	// ------------------------------

	public SchemaPlugin(String schema) {
		this.schema = schema;
	}

	// ------------------------------

	@Override
	public String process(String query, Map<String, Object> params) {
		return applySchema(schema, query);
	}

	// ------------------------------

	public static String applySchema(String schema, String query) {
		StringBuffer builder = new StringBuffer();
		Matcher matcher = SCHEMA_PATTERN.matcher(query);
		while (matcher.find()) {
			if (matcher.group(6) != null && matcher.group(7) != null) {
				String replacement;
				if (matcher.group(7).contains(".") || KEYWORDS.contains(matcher.group(7).toLowerCase()))
					replacement = String.format("%s %s", matcher.group(6), matcher.group(7));
				else
					replacement = String.format("%s %s.%s", matcher.group(6), schema, matcher.group(7));
				matcher.appendReplacement(builder, replacement);
			}
		}
		matcher.appendTail(builder);
		return builder.toString();
	}
}
