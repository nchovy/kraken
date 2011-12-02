package org.krakenapps.logdb.query.parser;

import static org.krakenapps.bnf.Syntax.choice;
import static org.krakenapps.bnf.Syntax.k;
import static org.krakenapps.bnf.Syntax.ref;
import static org.krakenapps.bnf.Syntax.rule;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.query.OptionPlaceholder;
import org.krakenapps.logdb.query.command.OptionChecker;

public class OptionCheckerParser implements LogQueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		// @formatter:off
		SingleOptionCheckerParser s = new SingleOptionCheckerParser();
		MultipleOptionCheckerParser m = new MultipleOptionCheckerParser();
		syntax.add("option_checker_single", s, choice(rule(k("("), ref("option_checker_single"), k(")")), new OptionPlaceholder()));
		syntax.add("option_checker_multi1", m, choice(rule(k("("), ref("option_checker_multi1"), k(")")), rule(ref("option_checker_single"),
				choice(k("and"), k("or")), choice(ref("option_checker_multi1"), ref("option_checker_single")))));
		syntax.add("option_checker_multi2", m, choice(rule(k("("), ref("option_checker_multi2"), k(")")), rule(ref("option_checker_multi1"),
				choice(k("and"), k("or")), choice(ref("option_checker_multi1"), ref("option_checker_single")))));
		syntax.add("option_checker", this, choice(ref("option_checker_single"), ref("option_checker_multi1"), ref("option_checker_multi2")));
		// @formatter:on
	}

	@Override
	public Object parse(Binding b) {
		return b.getValue();
	}

	private class SingleOptionCheckerParser implements Parser {
		@Override
		public Object parse(Binding b) {
			if (b.getChildren() != null && b.getChildren().length == 3)
				return b.getChildren()[1].getValue();
			return b.getValue();
		}
	}

	private class MultipleOptionCheckerParser implements Parser {
		@Override
		public Object parse(Binding b) {
			if (b.getChildren()[0].getValue().equals("(")) {
				OptionChecker result = (OptionChecker) b.getChildren()[1].getValue();
				result.setBracket(true);
				return result;
			}

			OptionChecker lh = (OptionChecker) b.getChildren()[0].getValue();
			boolean isAnd = "and".equalsIgnoreCase((String) b.getChildren()[1].getValue());
			OptionChecker rh = (OptionChecker) b.getChildren()[2].getValue();
			if (isAnd) {
				if (!lh.isHighPriority() && !rh.isHighPriority()) // {A|B}&{C|D}=>(A|(B&C))|D
					return new OptionChecker(new OptionChecker(lh.getLh2(), false, new OptionChecker(lh.getRh2(), true, rh.getLh2())),
							false, rh.getRh2());
				else if (!lh.isHighPriority()) // {A|B}&C=>A|(B&C)
					return new OptionChecker(lh.getLh2(), false, new OptionChecker(lh.getRh2(), true, rh));
				else if (!rh.isHighPriority()) // A&{B|C}=>(A&B)|C
					return new OptionChecker(new OptionChecker(lh, true, rh.getLh2()), false, rh.getRh2());
			}
			return new OptionChecker(lh, isAnd, rh);
		}
	}
}
