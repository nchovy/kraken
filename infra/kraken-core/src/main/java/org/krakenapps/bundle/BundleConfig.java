/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.bundle;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.krakenapps.api.BundleRepository;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class BundleConfig {
	private static final String BUNDLE_REPO_PATH = "/bundle/repo";
	private Preferences prefs;

	public BundleConfig(Preferences prefs) {
		this.prefs = prefs;
		initialize();
	}

	private void initialize() {
		try {
			createBundleRepositoriesTable();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	public List<BundleRepository> getBundleRepositories() {
		Preferences repos = getRootPreferences();
		List<BundleRepository> repositories = new ArrayList<BundleRepository>();
		String[] childrenNames;
		try {
			childrenNames = repos.childrenNames();
		} catch (BackingStoreException e) {
			return repositories;
		}

		for (String key : childrenNames) {
			try {
				Preferences repo = repos.node(key);
				URL url = new URL(repo.get("url", null));

				BundleRepository config = new BundleRepository(key, url);
				config.setAuthRequired(repo.getBoolean("auth", false));
				config.setAccount(repo.get("account", null));
				config.setPassword(repo.get("password", null));
				config.setPriority(repo.getInt("priority", 0));
				config.setTrustStoreAlias(repo.get("truststore", null));
				config.setKeyStoreAlias(repo.get("keystore", null));

				repositories.add(config);
			} catch (MalformedURLException e) {
			}
		}

		return repositories;
	}

	public void addRepository(String name, URL url, int priority) throws BackingStoreException {
		Preferences repos = getRootPreferences();
		Preferences newNode = repos.node(name);
		newNode.put("url", url.toString());
		newNode.putInt("priority", priority);

		sync(newNode);
		sync(repos);
	}

	public void addSecureRepository(String name, URL url, String trustStoreAlias, String keyStoreAlias)
			throws BackingStoreException {
		Preferences repos = getRootPreferences();
		Preferences newNode = repos.node(name);
		newNode.put("url", url.toString());
		newNode.put("truststore", trustStoreAlias);

		if (keyStoreAlias != null)
			newNode.put("keystore", keyStoreAlias);

		sync(newNode);
		sync(repos);
	}

	private Preferences getRootPreferences() {
		Preferences repos = prefs.node(BUNDLE_REPO_PATH);
		return repos;
	}

	public void removeRepository(String name) throws BackingStoreException {
		Preferences repos = getRootPreferences();
		Preferences repo = repos.node(name);
		repo.removeNode();
		sync(repos);
	}

	private void sync(Preferences p) throws BackingStoreException {
		p.flush();
		p.sync();
	}

	private void createBundleRepositoriesTable() throws BackingStoreException {
		try {
			addRepository("krakenapps", new URL("http://download.krakenapps.org/"), 1);
			addRepository("maven", new URL("http://repo1.maven.org/maven2/"), 0);
		} catch (MalformedURLException e) {
		}
	}
}
