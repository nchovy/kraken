package org.krakenapps.logdb.jython;

import java.util.Collection;

import org.krakenapps.logdb.LogScript;

public interface JythonLogScriptRegistry {
	Collection<String> getScriptNames();

	String getScript(String name);

	LogScript getLogScript(String name);

	void addScript(String name, String script);

	void updateScript(String name, String script);

	void removeScript(String name);
}
