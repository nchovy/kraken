package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.command.Drop;

public class DropParser implements QueryParser {
	@Override
	public Object parse(Binding b) {
		return new Drop();
	}

	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("drop", this, k("drop"));
		syntax.addRoot("drop");
	}
}
