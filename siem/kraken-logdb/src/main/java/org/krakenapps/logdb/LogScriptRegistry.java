package org.krakenapps.logdb;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LogScriptRegistry {
	Set<String> getWorkspaceNames();
	
	void createWorkspace(String name);

	void dropWorkspace(String name);

	Set<String> getScriptFactoryNames(String workspace);

	List<LogScriptFactory> getScriptFactories(String workspace);
	
	LogScriptFactory getScriptFactory(String workspace, String name);

	LogScript newScript(String workspace, String name, Map<String, Object> params);

	void addScriptFactory(String workspace, String name, LogScriptFactory factory);

	void removeScriptFactory(String workspace, String name);
}
