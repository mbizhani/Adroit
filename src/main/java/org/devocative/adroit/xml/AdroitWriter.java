package org.devocative.adroit.xml;

import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

import java.io.Writer;

public class AdroitWriter extends PrettyPrintWriter {

	private boolean outputCDATA;
	private boolean compact = false;

	// ------------------------------

	public AdroitWriter(Writer writer, NameCoder nameCoder) {
		super(writer, XML_QUIRKS, new char[]{'\t'}, nameCoder);
	}

	// ------------------------------

	public boolean isCompact() {
		return compact;
	}

	public AdroitWriter setCompact(boolean compact) {
		this.compact = compact;
		return this;
	}

	// ------------------------------
	@Override
	public void startNode(String name, Class clazz) {
		// Uses the default startNode
		super.startNode(name, clazz);

		// Checks if the field to be serialized is an String
		outputCDATA = clazz.equals(String.class);
	}

	@Override
	protected void writeText(QuickWriter writer, String text) {
		// If the field is and String then wrap around CDATA

		if (text != null) {
			if (outputCDATA) {
				writer.write("<![CDATA[");
				writer.write(text);
				writer.write("]]>");
			} else {
				writer.write(text);
			}
		}
	}

	@Override
	protected void endOfLine() {
		if (!isCompact()) {
			super.endOfLine();
		}
	}
}
