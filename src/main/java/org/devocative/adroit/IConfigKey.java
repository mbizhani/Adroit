package org.devocative.adroit;

import java.util.List;

public interface IConfigKey {
	String getKey();

	boolean getValidate();

	Object getDefaultValue();

	List<?> getPossibleValues();
}
