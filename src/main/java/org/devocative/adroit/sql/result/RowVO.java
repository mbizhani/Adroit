package org.devocative.adroit.sql.result;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.collections.MapConverter;

import java.util.LinkedHashMap;

@XStreamConverter(MapConverter.class)
public class RowVO extends LinkedHashMap<String, Object> {
	private static final long serialVersionUID = 8800701053699750620L;
}
