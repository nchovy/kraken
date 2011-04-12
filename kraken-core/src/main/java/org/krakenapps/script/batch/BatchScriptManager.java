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
import java.io.InputStreamReader;
import java.util.List;

import org.krakenapps.api.ScriptContext;
import org.krakenapps.console.ScriptRunner;
import org.krakenapps.main.Kraken;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;

public class BatchScriptManager {
	private BatchScriptConfig config;

	public BatchScriptManager() {
		config = new BatchScriptConfig(getSystemPreferences());
	}

	public void register(String alias, File scriptFile) {
		config.addBatchMapping(alias, scriptFile);
	}

	public void unregister(String alias) {
		config.removeBatchMapping(alias);
	}

	public File getPath(String alias) {
		return config.getScriptPath(alias);
	}

	public List<BatchMapping> getBatchMappings() {
		return config.getBatchMappings();
	}

	public void execute(ScriptContext context, String alias) throws Exception {
		execute(context, alias, true);
	}

	public void execute(ScriptContext context, String alias, boolean stopOnFail) throws Exception {
		File scriptFile = config.getScriptPath(alias);
		if (scriptFile == null)
			throw new Exception("script not found");

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(scriptFile)));
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;

			try {
				context.printf("executing \"%s\"\n", line);
				ScriptRunner runner = new ScriptRunner(context, line);
				runner.setPrompt(false);
				runner.run();
			} catch (Exception e) {
				context.println(e.getMessage());
				if (stopOnFail)
					break;
			}

		}
	}

	private Preferences getSystemPreferences() {
		BundleContext bc = Kraken.getContext();
		ServiceReference ref = bc.getServiceReference(PreferencesService.class.getName());
		PreferencesService prefsService = (PreferencesService) bc.getService(ref);
		return prefsService.getSystemPreferences();
	}
}
