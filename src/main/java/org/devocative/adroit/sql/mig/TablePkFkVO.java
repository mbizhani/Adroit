package org.devocative.adroit.sql.mig;

import java.util.List;

public class TablePkFkVO {
	private String name;
	private String pkConstraint;
	private String pkColumn;
	private List<RefConsVO> referencedBy;

	// ------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPkConstraint() {
		return pkConstraint;
	}

	public void setPkConstraint(String pkConstraint) {
		this.pkConstraint = pkConstraint;
	}

	public String getPkColumn() {
		return pkColumn;
	}

	public void setPkColumn(String pkColumn) {
		this.pkColumn = pkColumn;
	}

	public List<RefConsVO> getReferencedBy() {
		return referencedBy;
	}

	public void setReferencedBy(List<RefConsVO> referencedBy) {
		this.referencedBy = referencedBy;
	}
}
