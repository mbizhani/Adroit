package org.devocative.adroit.xml;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.Xpp3Driver;

import java.io.Writer;

class AdroitXppDriver extends Xpp3Driver {
	private boolean compact = false;

	public AdroitXppDriver(boolean compact) {
		this.compact = compact;
	}

	@Override
	public HierarchicalStreamWriter createWriter(Writer out) {
		return new AdroitWriter(out, getNameCoder()).setCompact(compact);
	}
}