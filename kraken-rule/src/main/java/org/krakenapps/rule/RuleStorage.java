package org.krakenapps.rule;

import java.util.Collection;

public interface RuleStorage {
	String getName();

	Collection<RuleGroup> getRuleGroups();

	RuleGroup getRuleGroup(String name);
}
