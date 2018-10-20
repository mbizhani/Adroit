package org.devocative.adroit.sql.sort;

public enum SortType {
	ASC("asc"), DESC("desc");

	private final String name;

	SortType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
