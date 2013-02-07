/*
 * Copyright 2012 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.logdb.jython.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.jython.JythonService;
import org.krakenapps.logdb.LogScript;
import org.krakenapps.logdb.LogScriptFactory;
import org.krakenapps.logdb.LogScriptRegistry;
import org.krakenapps.logdb.jython.JythonLogScriptRegistry;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "jython-logscript-registry")
@Provides
public class JythonLogScriptRegistryImpl implements JythonLogScriptRegistry {
	private final Logger logger = LoggerFactory.getLogger(JythonLogScriptRegistryImpl.class);

	@Requires
	private ConfigService conf;

	@Requires
	private JythonService jython;

	@Requires
	private LogScriptRegistry logScriptRegistry;

	private ConcurrentMap<String, ConcurrentMap<String, PyObject>> workspaceScripts;

	@Validate
	public void start() {
		workspaceScripts = new ConcurrentHashMap<String, ConcurrentMap<String, PyObject>>();

		// load scripts
		ConfigDatabase db = conf.ensureDatabase("kraken-logdb-jython");
		ConfigCollection col = db.ensureCollection("scripts");
		ConfigIterator it = col.findAll();
		try {
			for (LogScriptConfig sc : it.getDocuments(LogScriptConfig.class)) {
				try {
					PyObject o = eval(sc.name, sc.script);

					ConcurrentMap<String, PyObject> scripts = new ConcurrentHashMap<String, PyObject>();
					ConcurrentMap<String, PyObject> old = workspaceScripts.putIfAbsent(sc.workspace, scripts);
					if (old != null)
						scripts = old;

					scripts.put(sc.name, o);
				} catch (Throwable t) {
					logger.error("kraken logdb jython: cannot load script [" + sc.name + "]", t);
				}
			}
		} finally {
			it.close();
		}

		// register all
		for (String workspace : workspaceScripts.keySet()) {
			ConcurrentMap<String, PyObject> scripts = workspaceScripts.get(workspace);
			for (String name : scripts.keySet())
				try {
					PyObject factory = scripts.get(name);
					logScriptRegistry.addScriptFactory(workspace, name, new LogScriptFactoryImpl(factory));
				} catch (Throwable t) {
					logger.error("kraken logdb jython: cannot register script [" + name + "]", t);
				}
		}
	}

	@Invalidate
	public void stop() {
		if (logScriptRegistry != null) {
			for (String workspace : workspaceScripts.keySet()) {
				ConcurrentMap<String, PyObject> scripts = workspaceScripts.get(workspace);
				for (String name : scripts.keySet())
					logScriptRegistry.removeScriptFactory(workspace, name);
			}
		}

		if (jython != null)
			jython.unregisterInterpreter("logdb");
	}

	@Override
	public Set<String> getWorkspaceNames() {
		return Collections.unmodifiableSet(workspaceScripts.keySet());
	}

	@Override
	public void dropWorkspace(String name) {

	}

	@Override
	public Set<String> getScriptNames(String workspace) {
		ConcurrentMap<String, PyObject> scripts = workspaceScripts.get(workspace);
		if (scripts == null)
			return null;
		return scripts.keySet();
	}

	@Override
	public String getScriptCode(String workspace, String name) {
		ConfigDatabase db = conf.ensureDatabase("kraken-logdb-jython");
		ConfigCollection col = db.ensureCollection("scripts");

		Config c = col.findOne(getPredicate(workspace, name));
		if (c == null)
			return null;

		LogScriptConfig sc = c.getDocument(LogScriptConfig.class);
		return sc.script;
	}

	@Override
	public LogScript newLogScript(String workspace, String name, Map<String, Object> params) {
		ConcurrentMap<String, PyObject> scripts = workspaceScripts.get(workspace);
		if (scripts == null)
			return null;

		PyObject factory = scripts.get(name);
		if (factory == null)
			return null;

		PyObject instance = factory.__call__();
		return (LogScript) instance.__tojava__(LogScript.class);
	}

	@Override
	public void setScript(String workspace, String name, String script) {
		PyObject factory = eval(name, script);

		ConcurrentMap<String, PyObject> scripts = new ConcurrentHashMap<String, PyObject>();
		ConcurrentMap<String, PyObject> old = workspaceScripts.putIfAbsent(workspace, scripts);
		if (old != null)
			scripts = old;

		scripts.put(name, factory);

		ConfigDatabase db = conf.ensureDatabase("kraken-logdb-jython");
		ConfigCollection col = db.ensureCollection("scripts");
		Config c = col.findOne(getPredicate(workspace, name));

		if (c == null) {
			LogScriptConfig sc = new LogScriptConfig(workspace, name, script);
			col.add(PrimitiveConverter.serialize(sc));

			// add to logdb script registry
			logScriptRegistry.addScriptFactory(workspace, name, new LogScriptFactoryImpl(factory));
		} else {
			LogScriptConfig sc = new LogScriptConfig(workspace, name, script);
			c.setDocument(PrimitiveConverter.serialize(sc));
			col.update(c);
			
			logScriptRegistry.removeScriptFactory(workspace, name);
			logScriptRegistry.addScriptFactory(workspace, name, new LogScriptFactoryImpl(factory));
		}
	}

	@Override
	public void removeScript(String workspace, String name) {
		ConfigDatabase db = conf.ensureDatabase("kraken-logdb-jython");
		ConfigCollection col = db.ensureCollection("scripts");
		Config c = col.findOne(Predicates.field("name", name));
		if (c == null)
			throw new IllegalStateException("script not found: " + name);

		col.remove(c);

		// remove from memory
		ConcurrentMap<String, PyObject> scripts = workspaceScripts.get(workspace);
		if (scripts == null)
			return;

		scripts.remove(name);

		// remove from logdb script registry
		logScriptRegistry.removeScriptFactory(workspace, name);
	}

	private PyObject eval(String name, String script) {
		PythonInterpreter interpreter = jython.newInterpreter();
		interpreter.exec("from org.krakenapps.logdb import LogScript");
		interpreter.exec("from org.krakenapps.logdb import BaseLogScript");
		interpreter.exec(script);
		PyObject clazz = interpreter.get(name);
		if (clazz == null)
			throw new IllegalStateException("cannot eval jython script " + name);

		return clazz;
	}

	private Predicate getPredicate(String workspace, String name) {
		Map<String, Object> pred = new HashMap<String, Object>();
		pred.put("name", name);
		pred.put("workspace", workspace);
		return Predicates.field(pred);
	}

	private static class LogScriptConfig {
		private String workspace;
		private String name;
		private String script;

		@SuppressWarnings("unused")
		public LogScriptConfig() {
		}

		public LogScriptConfig(String workspace, String name, String script) {
			this.workspace = workspace;
			this.name = name;
			this.script = script;
		}
	}

	private static class LogScriptFactoryImpl implements LogScriptFactory {

		private PyObject factory;

		public LogScriptFactoryImpl(PyObject factory) {
			this.factory = factory;
		}

		@Override
		public LogScript create(Map<String, Object> params) {
			PyObject instance = factory.__call__();
			return (LogScript) instance.__tojava__(LogScript.class);
		}

		@Override
		public String getDescription() {
			PyObject __doc__ = factory.getDoc();
			String doc = (String) __doc__.__tojava__(String.class);
			if (doc == null)
				return "N/A";
			
			return doc.trim();
		}

		@Override
		public String toString() {
			return getDescription();
		}
	}

}
