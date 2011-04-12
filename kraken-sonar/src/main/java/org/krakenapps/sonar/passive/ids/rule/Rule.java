package org.krakenapps.sonar.passive.ids.rule;

import java.util.List;

public class Rule {
	private String id;
	private String name;
	private List<RuleOption> optionList;

	public Rule(List<RuleOption> options) {
		optionList = options;
		id = find(optionList, "id");
		name = find(optionList, "name");
	}

	public String find(String name) {
		for (RuleOption o : this.optionList)
			if (o.getName().equals(name))
				return o.getValue();
		return null;
	}
	private String find(List<RuleOption> options, String name) {
		for (RuleOption o : options)
			if (o.getName().equals(name))
				return o.getValue();

		return null;
	}

	@Override
	public String toString() {
		return String.format("SONAR-RULE/%s (%s)", id, name);
	}

}
