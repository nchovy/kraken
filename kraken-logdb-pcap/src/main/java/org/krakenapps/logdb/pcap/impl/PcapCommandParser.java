package org.krakenapps.logdb.pcap.impl;

import static org.krakenapps.bnf.Syntax.k;

import java.io.File;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQueryParser;

public class PcapCommandParser implements LogQueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("pcap", this, k("pcap"), new StringPlaceholder());
		syntax.addRoot("pcap");
	}

	@Override
	public Object parse(Binding b) {
		String filename = (String) b.getChildren()[1].getValue();
		return new PcapCommand(new File(filename));
	}

}
