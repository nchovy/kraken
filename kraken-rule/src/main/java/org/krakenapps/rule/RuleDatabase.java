package org.krakenapps.rule;

import java.util.Collection;

public interface RuleDatabase {
	Collection<RuleStorage> getStorages();

	Collection<RuleGroup> getRuleGroups(String name);

	void addStorage(RuleStorage storage);

	void removeStorage(RuleStorage storage);
}
