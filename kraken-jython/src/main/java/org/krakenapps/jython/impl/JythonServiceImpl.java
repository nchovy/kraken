/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.jython.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.jython.JythonService;
import org.osgi.framework.BundleContext;
import org.python.core.PySystemState;
import org.python.util.InteractiveConsole;
import org.python.util.PythonInterpreter;

@Component(name = "jython-service")
@Provides
public class JythonServiceImpl implements JythonService {
	private BundleContext bc;
	private Map<String, PythonInterpreter> m = new ConcurrentHashMap<String, PythonInterpreter>();

	public JythonServiceImpl(BundleContext bc) {
		this.bc = bc;
	}

	@Override
	public PythonInterpreter getInterpreter(String key) {
		return m.get(key);
	}

	@Override
	public PythonInterpreter newInterpreter() {
		PythonInterpreter interpreter = new InteractiveConsole();
		interpreter.set("bc", bc);
		interpreter.exec("import sys");
		PySystemState sys = (PySystemState) interpreter.get("sys");
		sys.setClassLoader(getClass().getClassLoader());
		return interpreter;
	}

	@Override
	public void registerInterpreter(String key, PythonInterpreter interpreter) {
		m.put(key, interpreter);
	}

	@Override
	public void unregisterInterpreter(String key) {
		m.remove(key);
	}
}
