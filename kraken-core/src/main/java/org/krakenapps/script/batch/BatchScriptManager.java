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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.krakenapps.api.ScriptContext;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.console.ScriptRunner;
import org.krakenapps.main.Kraken;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class BatchScriptManager {
	private ConfigService conf;

	public BatchScriptManager() {
		BundleContext bc = Kraken.getContext();
		ServiceReference ref = bc.getServiceReference(ConfigService.class.getName());
		this.conf = (ConfigService) bc.getService(ref);
	}

	private ConfigDatabase getDatabase() {
		return conf.ensureDatabase("kraken-core");
	}

	public void register(String alias, File scriptFile) {
		getDatabase().add(new BatchMapping(alias, scriptFile));
	}

	public void unregister(String alias) {
		ConfigDatabase db = getDatabase();
		Config c = db.findOne(BatchMapping.class, Predicates.field("alias", alias));
		if (c != null)
			db.remove(c);
	}

	public File getPath(String alias) {
		ConfigDatabase db = getDatabase();
		Config c = db.findOne(BatchMapping.class, Predicates.field("alias", alias));
		if (c == null)
			return null;
		return new File(c.getDocument(BatchMapping.class).getFilepath());
	}

	public List<BatchMapping> getBatchMappings() {
		List<BatchMapping> mappings = new ArrayList<BatchMapping>();
		ConfigDatabase db = getDatabase();
		for (BatchMapping mapping : db.findAll(BatchMapping.class).getDocuments(BatchMapping.class)) {
			mapping.setScriptFileFromFilepath();
			mappings.add(mapping);
		}
		return mappings;
	}

	public void execute(ScriptContext context, String alias) throws IOException {
		execute(context, alias, true);
	}

	public void execute(ScriptContext context, String alias, boolean stopOnFail) throws IOException {
		File scriptFile = getPath(alias);
		if (scriptFile == null)
			throw new IOException("script not found");
		executeFile(context, scriptFile, stopOnFail);
	}

	public void executeFile(ScriptContext context, File file) throws IOException {
		executeFile(context, file, true);
	}

	public void executeFile(ScriptContext context, File file, boolean stopOnFail) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
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
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
		}
	}
}
