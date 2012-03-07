package org.krakenapps.logdb;

import java.util.Collection;

public interface LogScriptRegistry {
	Collection<LogScript> getScripts();

	LogScript getScript(String name);

	void addScript(String name, LogScript script);

	void removeScript(String name);
}
