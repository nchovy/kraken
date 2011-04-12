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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class BatchScriptConfig {
	private Preferences prefs;

	public BatchScriptConfig(Preferences prefs) {
		// mark root preferences node
		this.prefs = prefs;
	}

	public List<BatchMapping> getBatchMappings() {
		Preferences prefs = getBatchPreferences();
		List<BatchMapping> mappings = new ArrayList<BatchMapping>();

		try {
			for (String name : prefs.childrenNames()) {
				Preferences node = prefs.node(name);
				String path = node.get("path", null);
				BatchMapping mapping = new BatchMapping(name, new File(path));
				mappings.add(mapping);
			}
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}

		return mappings;
	}

	public File getScriptPath(String alias) {
		Preferences prefs = getBatchPreferences();
		try {
			if (!prefs.nodeExists(alias))
				return null;

			return new File(prefs.node(alias).get("path", null));
		} catch (BackingStoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void addBatchMapping(String alias, File scriptFile) {
		Preferences prefs = getBatchPreferences();
		try {
			if (prefs.nodeExists(alias))
				throw new IllegalStateException("duplicated name: " + alias);

			Preferences newBatch = prefs.node(alias);
			newBatch.put("path", scriptFile.getAbsolutePath());
			newBatch.flush();
			newBatch.sync();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	public void removeBatchMapping(String alias) {
		Preferences prefs = getBatchPreferences();
		try {
			if (prefs.nodeExists(alias)) {
				Preferences n = prefs.node(alias);
				n.removeNode();
			}
			
			prefs.flush();
			prefs.sync();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	private Preferences getBatchPreferences() {
		String account = "root";
		return prefs.node("/account").node(account).node("batch");
	}
}
