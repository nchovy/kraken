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
package org.krakenapps.script.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.krakenapps.api.PathAutoCompleter;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;

public class BatchScript implements Script {
	private ScriptContext context;
	private BatchScriptManager manager;

	public BatchScript(BatchScriptManager manager) {
		this.manager = manager;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "", arguments = {})
	public void list(String[] args) {
		context.println("Batch Scripts");
		context.println("=============");
		for (BatchMapping mapping : manager.getBatchMappings()) {
			context.println(mapping.toString());
		}
	}

	@ScriptUsage(description = "preview content of the batch script file", arguments = { @ScriptArgument(name = "alias") })
	public void view(String[] args) {
		String alias = args[0];
		File file = manager.getPath(alias);
		if (file == null) {
			context.println("alias not found: " + alias);
			return;
		}

		if (!file.exists() || !file.isFile()) {
			context.println("file not found: " + file.getAbsolutePath());
			return;
		}

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				context.println(line.trim());
			}
		} catch (FileNotFoundException e) {
			context.println("file not found: " + file.getAbsolutePath());
		} catch (IOException e) {
			context.printf("io exception: %s\n", e.toString());
		}

	}

	@ScriptUsage(description = "", arguments = { @ScriptArgument(name = "alias"), @ScriptArgument(name = "absolute path") })
	public void register(String[] args) {
		String alias = args[0];
		File scriptFile = new File(args[1]);

		try {
			manager.register(alias, scriptFile);
			context.println(alias + " registered.");
		} catch (Exception e) {
			context.println("register failed: " + e.toString());
		}
	}

	@ScriptUsage(description = "", arguments = { @ScriptArgument(name = "alias") })
	public void unregister(String[] args) {
		String alias = args[0];
		try {
			manager.unregister(alias);
			context.println(alias + " unregistered.");
		} catch (Exception e) {
			context.println("unregister failed: " + e.toString());
		}
	}

	@ScriptUsage(description = "", arguments = {
			@ScriptArgument(name = "alias", type = "string", description = "alias for script file"),
			@ScriptArgument(name = "stop on fail", type = "boolean", description = "true if you want to stop script on fail", optional = true) })
	public void execute(String[] args) {
		try {
			boolean stopOnFail = true;
			if (args.length > 1)
				stopOnFail = Boolean.parseBoolean(args[1]);

			manager.execute(context, args[0], stopOnFail);
		} catch (Exception e) {
			context.printf("batch failed: %s\n", e.toString());
		}
	}

	@ScriptUsage(description = "", arguments = {
			@ScriptArgument(name = "file path", type = "string", description = "script file path", autocompletion = PathAutoCompleter.class),
			@ScriptArgument(name = "stop on fail", type = "boolean", description = "true if you want to stop script on fail", optional = true) })
	public void executeFile(String[] args) {
		try {
			boolean stopOnFail = true;
			if (args.length > 1)
				stopOnFail = Boolean.parseBoolean(args[1]);

			manager.executeFile(context, new File(args[0]), stopOnFail);
		} catch (Exception e) {
			context.printf("batch failed: %s\n", e.toString());
		}
	}
}
