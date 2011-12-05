package org.krakenapps.logdb.query.parser;

import static org.krakenapps.bnf.Syntax.k;
import static org.krakenapps.bnf.Syntax.option;
import static org.krakenapps.bnf.Syntax.ref;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.query.StringPlaceholder;
import org.krakenapps.logdb.query.command.Eval;
import org.krakenapps.logdb.query.command.Term;

public class EvalParser implements LogQueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("eval", this, k("eval "), ref("term"), option(k("as "), new StringPlaceholder()));
		syntax.addRoot("eval");
	}

	@Override
	public Object parse(Binding b) {
		Term term = (Term) b.getChildren()[1].getValue();
		String column = term.toString();
		if (b.getChildren().length == 3)
			column = (String) b.getChildren()[2].getChildren()[1].getValue();
		return new Eval(term, column);
	}
}
