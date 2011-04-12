package org.krakenapps.sonar.passive.ids.rule;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;

public class RuleOptionParser implements Parser {

	@Override
	public Object parse(Binding b) {
		String optionName = (String) b.getChildren()[0].getValue();
		String optionValue = (String) b.getChildren()[2].getValue();
		return new RuleOption(optionName, optionValue);
	}

}
