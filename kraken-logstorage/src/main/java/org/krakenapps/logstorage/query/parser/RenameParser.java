package org.krakenapps.logstorage.query.parser;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.logstorage.query.command.Rename;

public class RenameParser implements Parser {
	@Override
	public Object parse(Binding b) {
		String from = (String) b.getChildren()[1].getValue();
		String to = (String) b.getChildren()[3].getValue();
		return new Rename(from, to);
	}
}
