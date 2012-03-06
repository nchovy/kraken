package org.krakenapps.logdb.jython.impl;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.jython.JythonService;
import org.krakenapps.logdb.LogScript;
import org.krakenapps.logdb.LogScriptRegistry;
import org.krakenapps.logdb.jython.JythonLogScriptRegistry;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

@Component(name = "jython-logscript-registry")
@Provides
public class JythonLogScriptRegistryImpl implements JythonLogScriptRegistry {
	@Requires
	private ConfigService conf;

	@Requires
	private JythonService jython;

	@Requires
	private LogScriptRegistry logScriptRegistry;

	private PythonInterpreter interpreter;
	private ConcurrentMap<String, PyObject> scripts;

	@Validate
	public void start() {
		scripts = new ConcurrentHashMap<String, PyObject>();
		interpreter = jython.newInterpreter();
		jython.registerInterpreter("logdb", interpreter);

		// load scripts
		ConfigDatabase db = conf.ensureDatabase("kraken-logdb-jython");
		ConfigCollection col = db.ensureCollection("scripts");
		ConfigIterator it = col.findAll();
		try {
			for (LogScriptConfig sc : it.getDocuments(LogScriptConfig.class)) {
				PyObject o = eval(sc.name, sc.script);
				scripts.put(sc.name, o);
			}
		} finally {
			it.close();
		}

		// register all
		for (String name : scripts.keySet()) {
			LogScript s = getLogScript(name);
			logScriptRegistry.addScript(name, s);
		}
	}

	@Invalidate
	public void stop() {
		if (logScriptRegistry != null) {
			for (String name : scripts.keySet())
				logScriptRegistry.removeScript(name);
		}

		if (jython != null)
			jython.unregisterInterpreter("logdb");
	}

	@Override
	public Collection<String> getScriptNames() {
		return scripts.keySet();
	}

	@Override
	public String getScript(String name) {
		ConfigDatabase db = conf.ensureDatabase("kraken-logdb-jython");
		ConfigCollection col = db.ensureCollection("scripts");
		Config c = col.findOne(Predicates.field("name", name));
		if (c == null)
			return null;

		LogScriptConfig sc = c.getDocument(LogScriptConfig.class);
		return sc.script;
	}

	@Override
	public LogScript getLogScript(String name) {
		PyObject o = scripts.get(name);
		PyObject instance = o.__call__();
		return (LogScript) instance.__tojava__(LogScript.class);
	}

	@Override
	public void addScript(String name, String script) {
		ConfigDatabase db = conf.ensureDatabase("kraken-logdb-jython");
		ConfigCollection col = db.ensureCollection("scripts");
		LogScriptConfig sc = new LogScriptConfig(name, script);
		col.add(PrimitiveConverter.serialize(sc));
		scripts.put(name, eval(name, script));
	}

	@Override
	public void updateScript(String name, String script) {
		ConfigDatabase db = conf.ensureDatabase("kraken-logdb-jython");
		ConfigCollection col = db.ensureCollection("scripts");
		Config c = col.findOne(Predicates.field("name", name));
		if (c == null)
			throw new IllegalStateException("script not found: " + name);

		LogScriptConfig sc = new LogScriptConfig(name, script);
		c.setDocument(PrimitiveConverter.serialize(sc));
		col.update(c);

		scripts.put(name, eval(name, script));
	}

	@Override
	public void removeScript(String name) {
		scripts.remove(name);

		ConfigDatabase db = conf.ensureDatabase("kraken-logdb-jython");
		ConfigCollection col = db.ensureCollection("scripts");
		Config c = col.findOne(Predicates.field("name", name));
		if (c == null)
			throw new IllegalStateException("script not found: " + name);

		col.remove(c);
	}

	private PyObject eval(String name, String script) {
		interpreter.exec(script);
		return interpreter.get(name);
	}

	private static class LogScriptConfig {
		private String name;
		private String script;

		@SuppressWarnings("unused")
		public LogScriptConfig() {
		}

		public LogScriptConfig(String name, String script) {
			this.name = name;
			this.script = script;
		}
	}
}
