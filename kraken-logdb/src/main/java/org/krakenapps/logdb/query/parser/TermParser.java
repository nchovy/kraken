package org.krakenapps.logdb.query.parser;

import static org.krakenapps.bnf.Syntax.choice;
import static org.krakenapps.bnf.Syntax.k;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.query.StringPlaceholder;
import org.krakenapps.logdb.query.command.Term;
import org.krakenapps.logdb.query.command.Term.Operator;

public class TermParser implements LogQueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		// @formatter:off
		syntax.add("term", this, new StringPlaceholder(), 
				choice(k("=="), k("!="), k(">"), k("<"), k(">="), k("<="), k("contain"), k("regexp"), k("in")), 
				new StringPlaceholder());
		// @formatter:on
	}

	@Override
	public Object parse(Binding b) {
		Binding[] v = b.getChildren();
		Term term = new Term();
		String lh = v[0].getValue().toString();
		term.setLh(lh);
		if (lh.startsWith("\"") && lh.endsWith("\"")) {
			term.setLhString(true);
			term.setLh(lh.substring(1, lh.length() - 1));
		}

		if (v[1].getValue().equals("=="))
			term.setOperator(Operator.Eq);
		else if (v[1].getValue().equals("!="))
			term.setOperator(Operator.Neq);
		else if (v[1].getValue().equals(">"))
			term.setOperator(Operator.Gt);
		else if (v[1].getValue().equals("<"))
			term.setOperator(Operator.Lt);
		else if (v[1].getValue().equals(">="))
			term.setOperator(Operator.Ge);
		else if (v[1].getValue().equals("<="))
			term.setOperator(Operator.Le);
		else if (v[1].getValue().equals("contain"))
			term.setOperator(Operator.Contain);
		else if (v[1].getValue().equals("regexp"))
			term.setOperator(Operator.Regexp);
		else if (v[1].getValue().equals("in"))
			term.setOperator(Operator.In);

		String rh = v[2].getValue().toString();
		term.setRh(rh);
		if (rh.startsWith("\"") && rh.endsWith("\"")) {
			term.setRhString(true);
			term.setRh(rh.substring(1, rh.length() - 1));
		}
		return term;
	}
}
