package org.devocative.adroit;

import java.util.Arrays;
import java.util.List;

public enum TestConfigKey implements IConfigKey {
	OK(true, "int.key", Arrays.asList(123, 234, 356)),
	NOK(true, "test.choices", Arrays.asList("ok", "nok"));

	private String key;
	private boolean validate = false;
	private Object defaultValue;
	private List<?> possibleValues;

	TestConfigKey(boolean validate, String key, List<?> possibleValues) {
		this.validate = validate;
		this.key = key;
		this.possibleValues = possibleValues;
	}

	TestConfigKey(String key, Object defaultValue, List<Object> possibleValues) {
		this.key = key;
		this.defaultValue = defaultValue;
		this.possibleValues = possibleValues;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public boolean getValidate() {
		return validate;
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

	@Override
	public List<?> getPossibleValues() {
		return possibleValues;
	}
}
