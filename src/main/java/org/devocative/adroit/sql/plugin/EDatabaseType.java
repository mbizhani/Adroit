package org.devocative.adroit.sql.plugin;

// ------------------- Enum -------------------
public enum EDatabaseType {
	Oracle("oracledriver", "jdbc:oracle"),
	MySql("mysql", "jdbc:mysql"),
	HSQLDB("hsqldb", "jdbc:hsqldb"),
	Unknown("-", "-");

	private String driverHint;
	private String urlHint;

	EDatabaseType(String driverHint, String urlHint) {
		this.driverHint = driverHint;
		this.urlHint = urlHint;
	}

	public String getDriverHint() {
		return driverHint;
	}

	public String getUrlHint() {
		return urlHint;
	}
}
