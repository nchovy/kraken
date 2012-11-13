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
package org.krakenapps.pkg;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.krakenapps.api.AlreadyInstalledPackageException;
import org.krakenapps.api.BundleRequirement;
import org.krakenapps.api.PackageDescriptor;
import org.krakenapps.api.PackageRepository;
import org.krakenapps.api.Version;
import org.krakenapps.api.VersionRange;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;

/**
 * Package configurator for metadata management
 * 
 * @author xeraph
 * 
 */
public class PackageDatabase {
	private ConfigService conf;

	public PackageDatabase(ConfigService conf) {
		this.conf = conf;
		insertDefaultRepositories();
	}

	private void insertDefaultRepositories() {
		PackageRepository[] DEFAULT_REPOSITORIES = null;
		try {
			DEFAULT_REPOSITORIES = new PackageRepository[] { PackageRepository.create("krakenapps", new URL(
					"http://download.krakenapps.org/kraken/")) };
		} catch (MalformedURLException e) {
		}

		if (getPackageRepositories().size() > 0)
			return;

		for (PackageRepository repo : DEFAULT_REPOSITORIES) {
			try {
				createPackageRepository(repo);
			} catch (IllegalStateException e) {
				// ignore duplicated repos
			}
		}
	}

	public List<PackageRepository> getPackageRepositories() {

		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		Collection<PackageRepositoryConfig> repos = db.findAll(PackageRepositoryConfig.class).getDocuments(
				PackageRepositoryConfig.class);

		List<PackageRepository> repositories = new ArrayList<PackageRepository>();

		for (PackageRepositoryConfig repo : repos) {
			repositories.add(convert(repo));
		}
		return repositories;
	}

	public PackageRepository getPackageRepository(String alias) {
		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		Config c = db.findOne(PackageRepositoryConfig.class, Predicates.field("alias", alias));
		if (c == null)
			return null;

		return convert(c.getDocument(PackageRepositoryConfig.class));
	}

	private PackageRepository convert(PackageRepositoryConfig repo) {
		try {
			URL url = new URL(repo.getUrl());
			String alias = repo.getAlias();
			boolean auth = repo.isAuthRequired();
			String account = repo.getAccount();
			String password = repo.getPassword();
			String trustStoreAlias = repo.getTrustStoreAlias();
			String keyStoreAlias = repo.getKeyStoreAlias();

			if (url.getProtocol().equals("https"))
				return PackageRepository.createHttps(alias, url, trustStoreAlias, keyStoreAlias);
			else if (auth)
				return PackageRepository.createHttpAuth(alias, url, account, password);
			else
				return PackageRepository.create(alias, url);

		} catch (MalformedURLException e) {
		}
		return null;
	}

	public void createPackageRepository(PackageRepository repo) {
		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		Config c = db.findOne(PackageRepositoryConfig.class, Predicates.field("alias", repo.getAlias()));
		if (c != null)
			throw new IllegalStateException("duplicated repository name: " + repo.getAlias());

		PackageRepositoryConfig config = new PackageRepositoryConfig(repo);
		db.add(config);
	}

	public void updatePackageRepository(PackageRepository repo) {
		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		Config c = db.findOne(PackageRepositoryConfig.class, Predicates.field("alias", repo.getAlias()));
		if (c == null)
			throw new IllegalStateException("repository [" + repo.getAlias() + "] not found");

		PackageRepositoryConfig config = new PackageRepositoryConfig(repo);
		db.update(c, config);
	}

	public void removePackageRepository(String alias) {
		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		Config c = db.findOne(PackageRepositoryConfig.class, Predicates.field("alias", alias));
		if (c == null)
			throw new IllegalStateException("package repository [" + alias + "] not found");

		c.remove();
	}

	public List<PackageDescriptor> getInstalledPackages() {
		ConfigDatabase db = conf.ensureDatabase("kraken-core");

		List<PackageDescriptor> packages = new ArrayList<PackageDescriptor>();
		Collection<InstalledPackage> installedPkgs = db.findAll(InstalledPackage.class).getDocuments(InstalledPackage.class);

		for (InstalledPackage installed : installedPkgs) {
			PackageDescriptor pkg = toPackageObject(installed);
			packages.add(pkg);
		}

		return packages;
	}

	private PackageDescriptor toPackageObject(InstalledPackage pkg) {
		Date released = pkg.getReleased();
		Version version = new Version(pkg.getVersion());

		PackageDescriptor desc = new PackageDescriptor(pkg.getName(), version, released);

		for (PackageBundleRequirement req : pkg.getBundleRequirements()) {
			String name = req.getName();
			String lowVersion = req.getLowVersion();
			String highVersion = req.getHighVersion();

			VersionRange range = new VersionRange(new Version(lowVersion), new Version(highVersion));
			BundleRequirement bundleDesc = new BundleRequirement(name, range);
			desc.getBundleRequirements().add(bundleDesc);
		}

		return desc;
	}

	public PackageDescriptor findInstalledPackage(String name) {
		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		Config c = db.findOne(InstalledPackage.class, Predicates.field("name", name));
		if (c == null)
			return null;

		return toPackageObject(c.getDocument(InstalledPackage.class));
	}

	public void installPackage(PackageDescriptor pkg) throws AlreadyInstalledPackageException {
		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		Config c = db.findOne(InstalledPackage.class, Predicates.field("name", pkg.getName()));
		if (c != null)
			throw new IllegalStateException("package [" + pkg.getName() + "] already installed");

		InstalledPackage installed = new InstalledPackage();
		installed.setName(pkg.getName());
		installed.setVersion(pkg.getVersion().toString());
		installed.setReleased(pkg.getDate());
		installed.setDescription(pkg.getDescription());

		for (BundleRequirement bundleDesc : pkg.getBundleRequirements()) {
			PackageBundleRequirement req = new PackageBundleRequirement();
			req.setName(bundleDesc.getName());
			req.setLowVersion(bundleDesc.getVersionRange().getLow().toString());
			req.setHighVersion(bundleDesc.getVersionRange().getHigh().toString());
			installed.getBundleRequirements().add(req);
		}

		db.add(installed);
	}

	public void uninstallPackage(String name) {
		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		Config c = db.findOne(InstalledPackage.class, Predicates.field("name", name));
		if (c == null)
			throw new IllegalStateException("package [" + name + "] not found");

		c.remove();
	}

	public void updatePackage(PackageDescriptor pkg) {
		try {
			uninstallPackage(pkg.getName());
			installPackage(pkg);
		} catch (AlreadyInstalledPackageException e) {
			e.printStackTrace();
		}
	}
}
