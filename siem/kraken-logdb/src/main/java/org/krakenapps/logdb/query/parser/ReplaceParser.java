package org.krakenapps.logdb.query.parser;

import static org.krakenapps.bnf.Syntax.k;
import static org.krakenapps.bnf.Syntax.ref;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.query.StringPlaceholder;
import org.krakenapps.logdb.query.command.Replace;
import org.krakenapps.logdb.query.command.Term;

public class ReplaceParser implements LogQueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("replace", this, k("replace "), ref("option"), ref("term"), new StringPlaceholder());
		syntax.addRoot("replace");
	}

	@Override
	public Object parse(Binding b) {
		Term term = (Term) b.getChildren()[2].getValue();
		String value = (String) b.getChildren()[3].getValue();
		return new Replace(term, value);
	}
}
