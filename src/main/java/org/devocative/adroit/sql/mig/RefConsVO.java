package org.devocative.adroit.sql.mig;

public class RefConsVO {
	private String tableName;
	private String consName;
	private String columnName;

	// ------------------------------

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getConsName() {
		return consName;
	}

	public void setConsName(String consName) {
		this.consName = consName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
}
