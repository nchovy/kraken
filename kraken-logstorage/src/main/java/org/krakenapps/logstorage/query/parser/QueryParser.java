package org.krakenapps.logstorage.query.parser;

import org.krakenapps.bnf.Parser;
import org.krakenapps.bnf.Syntax;

public interface QueryParser extends Parser {
	void addSyntax(Syntax syntax);
}
