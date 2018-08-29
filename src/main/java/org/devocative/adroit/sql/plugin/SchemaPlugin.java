package org.devocative.adroit.sql.plugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchemaPlugin implements INpsPlugin {
	/*
	Patterns to ignore:
		(['].*?[']) ignore characters between two single quote (SQL string constant)
		(["].*?["]) ignore characters between two double quote (SQL identifier)
		(--.*?\n) ignore characters in single line comments
		(/[*].*?[*]/) ignore characters in inline comments
		(extract[(].+?[)]) ignore characters in extract() function: it has "from" in its syntax e.g. extract(day from ?)

	Main Pattern:
		(from|join|into|update)[\s]+(\w+([.]\w+)?) finding table name with schema if mentioned
	 */
	private static final String PATTERN =
		"(['].*?['])|([\"].*?[\"])|(--.*?\\n)|(/[*].*?[*]/)|(extract[(].+?[)])|(from|join|into|update)[\\s]+(\\w+([.]\\w+)?)";
	private static Pattern SCHEMA_PATTERN;

	private static final Set<String> KEYWORDS = new HashSet<>();

	static {
		SCHEMA_PATTERN = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE);

		KEYWORDS.add("set");
		KEYWORDS.add("dual");
	}

	// ------------------------------

	public static void set(String schemaPattern, Set<String> keywords) {
		SCHEMA_PATTERN = Pattern.compile(schemaPattern);

		KEYWORDS.clear();
		KEYWORDS.addAll(keywords);
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

	public static Pattern getSchemaPattern() {
		return Pattern.compile(PATTERN);
	}

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
