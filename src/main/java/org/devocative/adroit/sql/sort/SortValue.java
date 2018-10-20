package org.devocative.adroit.sql.sort;

public class SortValue {
	private String field;
	private SortType type;

	// ------------------------------

	public SortValue(String field, SortType type) {
		this.field = field;
		this.type = type;
	}

	// ------------------------------

	public String getField() {
		return field;
	}

	public SortType getType() {
		return type;
	}
}
