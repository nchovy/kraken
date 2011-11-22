package org.krakenapps.logdb.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.query.command.Drop;

public class DropParser implements LogQueryParser {
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
