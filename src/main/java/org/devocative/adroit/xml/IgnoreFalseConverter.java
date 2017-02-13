package org.devocative.adroit.xml;

import com.thoughtworks.xstream.converters.basic.BooleanConverter;

class IgnoreFalseConverter extends BooleanConverter {
	@Override
	public String toString(Object obj) {
		if (Boolean.FALSE.equals(obj)) {
			return null;
		}
		return super.toString(obj);
	}
}
