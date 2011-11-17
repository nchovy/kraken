package org.krakenapps.logdb.impl;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.logdb.LogScript;
import org.krakenapps.logdb.LogScriptRegistry;

@Component(name = "log-script-registry")
@Provides
public class LogScriptRegistryImpl implements LogScriptRegistry {

	private ConcurrentMap<String, LogScript> scripts;

	@Validate
	public void start() {
		scripts = new ConcurrentHashMap<String, LogScript>();
	}

	@Override
	public Collection<LogScript> getScripts() {
		return scripts.values();
	}

	@Override
	public LogScript getScript(String name) {
		return scripts.get(name);
	}

	@Override
	public void addScript(String name, LogScript script) {
		LogScript old = scripts.putIfAbsent(name, script);
		if (old != null)
			throw new IllegalStateException("log script already exists: " + name);
	}

	@Override
	public void removeScript(String name) {
		scripts.remove(name);
	}

}
