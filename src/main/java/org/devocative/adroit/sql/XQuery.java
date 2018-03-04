package org.devocative.adroit.sql;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("query")
public class XQuery {
	@XStreamAsAttribute
	private String name;

	private String sql;

	// ------------------------------

	public XQuery() {
	}

	public XQuery(String sql) {
		this.sql = sql;
	}

	// ------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String export) {
		this.sql = export;
	}

	// ------------------------------

	public static XQuery sql(String sql) {
		return new XQuery(sql);
	}
}
