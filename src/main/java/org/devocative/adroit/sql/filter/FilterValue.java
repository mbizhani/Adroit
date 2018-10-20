package org.devocative.adroit.sql.filter;

public class FilterValue {
	private final String field;
	private final Object value;
	private final Object lower;
	private final Object upper;
	private final FilterType type;

	private String sqlFunc;

	// ------------------------------

	public static FilterValue equal(String field, Object value) {
		return new FilterValue(field, value, FilterType.Equal);
	}

	public static FilterValue contain(String field, String value) {
		return new FilterValue(field, value, FilterType.Contain);
	}


	public static FilterValue between(String field, Object lower, Object upper) {
		return new FilterValue(field, lower, upper, FilterType.Between);
	}

	public static FilterValue range(String field, Object lower, Object upper) {
		return new FilterValue(field, lower, upper, FilterType.Range);
	}

	// ------------------------------

	private FilterValue(String field, Object value, FilterType type) {
		this.field = field;
		this.value = value;
		this.type = type;

		this.upper = null;
		this.lower = null;
	}

	private FilterValue(String field, Object lower, Object upper, FilterType type) {
		this.field = field;
		this.type = type;
		this.lower = lower;
		this.upper = upper;
		this.value = null;
	}

	// ------------------------------

	public String getField() {
		return field;
	}

	public Object getValue() {
		return value;
	}

	public Object getLower() {
		return lower;
	}

	public Object getUpper() {
		return upper;
	}

	public FilterType getType() {
		return type;
	}

	// ---------------

	public String getSqlFunc() {
		return sqlFunc;
	}

	public FilterValue setSqlFunc(String sqlFunc) {
		this.sqlFunc = sqlFunc;
		return this;
	}
}
