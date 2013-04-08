package org.krakenapps.logdb.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.logdb.LogScript;
import org.krakenapps.logdb.LogScriptFactory;
import org.krakenapps.logdb.LogScriptRegistry;

@Component(name = "log-script-registry")
@Provides
public class LogScriptRegistryImpl implements LogScriptRegistry {

	private ConcurrentMap<String, ConcurrentMap<String, LogScriptFactory>> workspaceToScripts;

	@Validate
	public void start() {
		workspaceToScripts = new ConcurrentHashMap<String, ConcurrentMap<String, LogScriptFactory>>();
	}

	@Override
	public Set<String> getWorkspaceNames() {
		return Collections.unmodifiableSet(workspaceToScripts.keySet());
	}

	@Override
	public void createWorkspace(String name) {
		workspaceToScripts.putIfAbsent(name, new ConcurrentHashMap<String, LogScriptFactory>());
	}

	@Override
	public void dropWorkspace(String name) {
		workspaceToScripts.remove(name);
	}

	@Override
	public Set<String> getScriptFactoryNames(String workspace) {
		ConcurrentMap<String, LogScriptFactory> scripts = workspaceToScripts.get(workspace);
		if (scripts == null)
			return null;

		return Collections.unmodifiableSet(scripts.keySet());
	}

	@Override
	public List<LogScriptFactory> getScriptFactories(String workspace) {
		ConcurrentMap<String, LogScriptFactory> scripts = workspaceToScripts.get(workspace);
		if (scripts == null)
			return null;

		return new ArrayList<LogScriptFactory>(scripts.values());
	}

	@Override
	public LogScriptFactory getScriptFactory(String workspace, String name) {
		ConcurrentMap<String, LogScriptFactory> scripts = workspaceToScripts.get(workspace);
		if (scripts == null)
			return null;
		
		return scripts.get(name);
	}

	@Override
	public LogScript newScript(String workspace, String name, Map<String, Object> params) {
		ConcurrentMap<String, LogScriptFactory> scripts = workspaceToScripts.get(workspace);
		if (scripts == null)
			throw new IllegalStateException("script not found: " + name);

		LogScriptFactory factory = scripts.get(name);
		if (factory == null)
			throw new IllegalStateException("script not found: " + name);
		
		return factory.create(params);
	}

	@Override
	public void addScriptFactory(String workspace, String name, LogScriptFactory factory) {
		ConcurrentMap<String, LogScriptFactory> scripts = new ConcurrentHashMap<String, LogScriptFactory>();
		ConcurrentMap<String, LogScriptFactory> oldScripts = workspaceToScripts.putIfAbsent(workspace, scripts);
		if (oldScripts != null)
			scripts = oldScripts;

		LogScriptFactory old = scripts.putIfAbsent(name, factory);
		if (old != null)
			throw new IllegalStateException("log script already exists: " + name);
	}

	@Override
	public void removeScriptFactory(String workspace, String name) {
		ConcurrentMap<String, LogScriptFactory> scripts = workspaceToScripts.get(workspace);
		if (scripts == null)
			return;

		scripts.remove(name);
	}
}
