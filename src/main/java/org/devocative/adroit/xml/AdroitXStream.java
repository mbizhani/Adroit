package org.devocative.adroit.xml;

import com.thoughtworks.xstream.XStream;

public class AdroitXStream extends XStream {
	public AdroitXStream() {
		this(false);
	}

	public AdroitXStream(boolean compactWriting) {
		super(new AdroitXppDriver(compactWriting));

		registerConverter(new IgnoreFalseConverter());
	}
}
