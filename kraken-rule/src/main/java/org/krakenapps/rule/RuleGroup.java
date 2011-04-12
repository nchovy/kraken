package org.krakenapps.rule;

import java.util.Collection;

public interface RuleGroup {
	RuleStorage getStorage();

	// http, smb, and so on
	String getName();

	Collection<Rule> getRules();
}
