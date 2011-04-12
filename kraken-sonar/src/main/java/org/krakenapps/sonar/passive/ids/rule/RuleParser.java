package org.krakenapps.sonar.passive.ids.rule;

import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;

public class RuleParser implements Parser {

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Binding b) {
		List<RuleOption> options = (List<RuleOption>) b.getChildren()[1].getValue();
		return new Rule(options);
	}

}
