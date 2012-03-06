package org.krakenapps.sonar.passive.ids.rule;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;

public class RuleBodyParser implements Parser {

	@Override
	public Object parse(Binding b) {
		List<RuleOption> options = new ArrayList<RuleOption>();
		parseOption(b, options);
		return options;
	}

	private void parseOption(Binding b, List<RuleOption> options) {
		if (b.getValue() != null && b.getValue() instanceof RuleOption){
			RuleOption option = (RuleOption) b.getValue();
			options.add(option);
		}
		
		if (b.getChildren() == null)
			return;

		for (Binding child : b.getChildren()) {
			parseOption(child, options);
		}
	}
}
