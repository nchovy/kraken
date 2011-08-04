package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.StringPlaceholder;
import org.krakenapps.logstorage.query.command.Eval;
import org.krakenapps.logstorage.query.command.Eval.Term;
import org.krakenapps.logstorage.query.command.Eval.Term.Operator;

public class EvalParser implements QueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add(
				"eval",
				new EvalParser(),
				k("eval"),
				option(k("limit"), k("="), uint()),
				repeat(rule(new StringPlaceholder(), choice(k("="), k("!="), k(">"), k("<"), k(">="), k("<=")),
						new StringPlaceholder())));
		syntax.addRoot("eval");
	}

	@Override
	public Object parse(Binding b) {
		Binding[] c = b.getChildren();
		Integer limit = null;
		int begin = 1;

		if (c[1].getChildren()[0].getValue().equals("limit")) {
			limit = (Integer) c[1].getChildren()[2].getValue();
			begin = 2;
		}

		List<Term> terms = new ArrayList<Eval.Term>();
		for (int i = begin; i < c.length; i++) {
			Binding[] v = c[i].getChildren();
			Term term = new Term();

			String lh = v[0].getValue().toString();
			term.setLh(lh);
			if (lh.startsWith("\"") && lh.endsWith("\"")) {
				term.setLhString(true);
				term.setLh(lh.substring(1, lh.length() - 1));
			}

			if (v[1].getValue().equals("="))
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

			String rh = v[2].getValue().toString();
			term.setRh(rh);
			if (rh.startsWith("\"") && rh.endsWith("\"")) {
				term.setRhString(true);
				term.setRh(rh.substring(1, rh.length() - 1));
			}

			terms.add(term);
		}

		return new Eval(limit, terms);
	}
}
