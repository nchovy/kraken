package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.StringPlaceholder;

public class OptionParser implements QueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("option", new OptionParser(),
				repeat(rule(new StringPlaceholder('='), k("="), new StringPlaceholder())));
	}

	@Override
	public Object parse(Binding b) {
		return null;
	}
}
