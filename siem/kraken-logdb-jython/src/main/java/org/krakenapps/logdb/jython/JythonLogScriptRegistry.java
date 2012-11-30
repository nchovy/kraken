package org.krakenapps.logdb.jython;

import java.util.Map;
import java.util.Set;

import org.krakenapps.logdb.LogScript;

public interface JythonLogScriptRegistry {
	Set<String> getWorkspaceNames();
	
	void dropWorkspace(String name);

	Set<String> getScriptNames(String workspace);

	String getScriptCode(String workspace, String name);

	LogScript newLogScript(String workspace, String name, Map<String, Object> params);

	void setScript(String workspace, String name, String script);

	void removeScript(String workspace, String name);
}
