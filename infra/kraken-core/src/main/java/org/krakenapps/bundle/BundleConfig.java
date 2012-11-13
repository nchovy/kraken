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
import java.util.Collection;
import java.util.List;

import org.krakenapps.api.BundleRepository;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleConfig {
	private final Logger logger = LoggerFactory.getLogger(BundleConfig.class);
	private ConfigService conf;

	public BundleConfig(ConfigService conf) {
		this.conf = conf;
		initialize();
	}

	private void initialize() {
		addDefaultBundleRepositories();
	}

	public List<BundleRepository> getRepositories() {
		List<BundleRepository> repositories = new ArrayList<BundleRepository>();
		ConfigDatabase db = conf.ensureDatabase("kraken-core");

		ConfigIterator it = db.findAll(BundleRepositoryConfig.class);
		Collection<BundleRepositoryConfig> configs = it.getDocuments(BundleRepositoryConfig.class);
		for (BundleRepositoryConfig c : configs) {
			try {
				URL url = new URL(c.getUrl());

				BundleRepository config = new BundleRepository(c.getName(), url);
				config.setAuthRequired(c.isAuthRequired());
				config.setAccount(c.getAccount());
				config.setPassword(c.getPassword());
				config.setPriority(c.getPriority());
				config.setTrustStoreAlias(c.getTrustStoreAlias());
				config.setKeyStoreAlias(c.getKeyStoreAlias());

				repositories.add(config);
			} catch (MalformedURLException e) {
				logger.error("kraken core: cannot fetch bundle repos", e);
			}
		}

		return repositories;
	}

	public BundleRepository getRepository(String alias) {
		ConfigDatabase db = conf.ensureDatabase("kraken-core");

		Config c = db.findOne(BundleRepositoryConfig.class, Predicates.field("name", alias));
		if (c == null)
			return null;

		try {
			BundleRepositoryConfig m = c.getDocument(BundleRepositoryConfig.class);
			BundleRepository repo = new BundleRepository(m.getName(), new URL(m.getUrl()));
			repo.setAuthRequired(m.isAuthRequired());
			repo.setAccount(m.getAccount());
			repo.setPassword(m.getPassword());
			repo.setPriority(m.getPriority());
			repo.setKeyStoreAlias(m.getKeyStoreAlias());
			repo.setTrustStoreAlias(m.getTrustStoreAlias());

			return repo;
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public void addRepository(BundleRepository repo) {
		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		Config c = db.findOne(BundleRepositoryConfig.class, Predicates.field("name", repo.getName()));
		if (c != null)
			throw new IllegalStateException("bundle repository [" + repo.getName() + "] already exists");

		db.add(new BundleRepositoryConfig(repo));
	}

	public void updateRepository(BundleRepository repo) {
		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		Config c = db.findOne(BundleRepositoryConfig.class, Predicates.field("name", repo.getName()));
		if (c == null)
			throw new IllegalStateException("bundle repository [" + repo.getName() + "] not found");

		db.update(c, new BundleRepositoryConfig(repo));
	}

	public void removeRepository(String name) {
		ConfigDatabase db = conf.ensureDatabase("kraken-core");

		Config c = db.findOne(BundleRepositoryConfig.class, Predicates.field("name", name));
		if (c != null)
			c.remove();
		else
			throw new IllegalStateException("bundle repository [" + name + "] not found");
	}

	private void addDefaultBundleRepositories() {
		try {
			addRepository(new BundleRepository("krakenapps", new URL("http://download.krakenapps.org/"), 1));
			addRepository(new BundleRepository("maven", new URL("http://repo1.maven.org/maven2/"), 0));
		} catch (MalformedURLException e) {
		} catch (IllegalStateException e) {
			// ignore duplicated repo
		}
	}
}