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

import java.io.File;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.jython.JythonService;
import org.osgi.framework.BundleContext;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.util.InteractiveConsole;
import org.python.util.PythonInterpreter;

public class JythonScript implements Script {
	private JythonService jython;
	private ScriptContext context;

	public JythonScript(BundleContext bc, JythonService jython) {
		this.jython = jython;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "load python script", arguments = {
			@ScriptArgument(name = "name", type = "string", description = "context name"),
			@ScriptArgument(name = "file", type = "string", description = "path") })
	public void load(String[] args) {
		String name = args[0];
		File file = new File(args[1]);

		PythonInterpreter interpreter = getInterpreter(name);
		JythonOutputStream os = new JythonOutputStream(context);
		interpreter.setOut(os);
		interpreter.setErr(os);
		interpreter.execfile(file.getAbsolutePath());
	}

	public void run(String[] args) {
		String name = null;
		if (args.length > 0)
			name = args[0];

		InteractiveConsole interpreter = getInterpreter(name);

		JythonOutputStream os = new JythonOutputStream(context);
		interpreter.setOut(os);
		interpreter.setErr(os);

		try {
			boolean more = false;
			while (true) {
				String line;
				try {
					context.print(more ? "... " : ">>> ");
					line = context.readLine();
				} catch (PyException exc) {
					if (!exc.match(Py.EOFError))
						throw exc;
					break;
				}
				more = interpreter.push(line);
			}
		} catch (InterruptedException e) {
			context.println("interrupted");
		}
	}

	private InteractiveConsole getInterpreter(String name) {
		InteractiveConsole interpreter;
		if (name == null) {
			name = "main";
			interpreter = (InteractiveConsole) jython.newInterpreter();
		} else {
			interpreter = (InteractiveConsole) jython.getInterpreter(name);
			if (interpreter == null) {
				interpreter = (InteractiveConsole) jython.newInterpreter();
				jython.registerInterpreter(name, interpreter);
			}
		}
		return interpreter;
	}
}
