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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.krakenapps.api.AlreadyInstalledPackageException;
import org.krakenapps.api.BundleRequirement;
import org.krakenapps.api.PackageDescriptor;
import org.krakenapps.api.PackageRepository;
import org.krakenapps.api.Version;
import org.krakenapps.api.VersionRange;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class PackageDatabase {
	private static final String BUNDLE_REQS = "bundle_reqs";
	private Preferences prefs;

	public PackageDatabase(Preferences prefs) throws BackingStoreException {
		this.prefs = prefs;
		insertDefaultRepositories();
	}

	private void insertDefaultRepositories() throws BackingStoreException {
		PackageRepository[] DEFAULT_REPOSITORIES = null;
		try {
			DEFAULT_REPOSITORIES = new PackageRepository[] { PackageRepository.create("krakenapps", new URL(
					"http://download.krakenapps.org/kraken/")) };
		} catch (MalformedURLException e) {
		}

		Preferences repoPrefs = getPackagePreferences().node("repo");
		if (repoPrefs.childrenNames().length > 0)
			return;

		for (PackageRepository repo : DEFAULT_REPOSITORIES) {
			Preferences p = repoPrefs.node(repo.getAlias());
			p.put("url", repo.getUrl().toString());
		}

		sync(repoPrefs);
	}

	private void sync(Preferences p) throws BackingStoreException {
		p.flush();
		p.sync();

	}

	public List<PackageRepository> getPackageRepositories() {
		List<PackageRepository> repositories = new ArrayList<PackageRepository>();

		Preferences repoPrefs = getPackagePreferences().node("repo");
		String[] childrenNames = null;
		try {
			childrenNames = repoPrefs.childrenNames();
		} catch (BackingStoreException e) {
			return repositories;
		}

		for (String alias : childrenNames) {
			try {
				Preferences p = repoPrefs.node(alias);
				URL url = new URL(p.get("url", null));
				boolean auth = p.getBoolean("auth", false);
				String account = p.get("account", null);
				String password = p.get("password", null);
				String trustStoreAlias = p.get("truststore", null);
				String keyStoreAlias = p.get("keystore", null);

				if (url.getProtocol().equals("https"))
					repositories.add(PackageRepository.createHttps(alias, url, trustStoreAlias, keyStoreAlias));
				else if (auth)
					repositories.add(PackageRepository.createHttpAuth(alias, url, account, password));
				else
					repositories.add(PackageRepository.create(alias, url));

			} catch (MalformedURLException e) {
			}
		}
		return repositories;
	}

	public void addPackageRepository(String alias, URL url) throws BackingStoreException {
		Preferences repoPrefs = getPackagePreferences().node("repo");
		if (repoPrefs.nodeExists(alias))
			throw new IllegalStateException("duplicated repository name");

		Preferences repo = repoPrefs.node(alias);
		repo.put("url", url.toString());
		sync(repoPrefs);
	}

	public void addSecurePackageRepository(String alias, URL url, String trustStoreAlias, String keyStoreAlias)
			throws BackingStoreException {
		Preferences repoPrefs = getPackagePreferences().node("repo");
		if (repoPrefs.nodeExists(alias))
			throw new IllegalStateException("duplicated repository name");

		Preferences repo = repoPrefs.node(alias);
		repo.put("url", url.toString());
		repo.put("truststore", trustStoreAlias);
		repo.put("keystore", keyStoreAlias);
		sync(repoPrefs);
	}

	public void removePackageRepository(String alias) throws BackingStoreException {
		Preferences repoPrefs = getPackagePreferences().node("repo");
		Preferences repo = repoPrefs.node(alias);
		repo.removeNode();
		sync(repoPrefs);
	}

	public List<PackageDescriptor> getInstalledPackages() {
		Preferences packagesPrefs = getPackagePreferences().node("installed");
		List<PackageDescriptor> packages = new ArrayList<PackageDescriptor>();

		String[] childNames = null;
		try {
			childNames = packagesPrefs.childrenNames();
		} catch (BackingStoreException e) {
			return packages;
		}

		for (String key : childNames) {
			Preferences packagePrefs = packagesPrefs.node(key);
			PackageDescriptor pkg = toPackageObject(key, packagePrefs);
			packages.add(pkg);
		}

		return packages;
	}

	private PackageDescriptor toPackageObject(String name, Preferences p) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			Date released = dateFormat.parse(p.get("released", null));
			Version version = new Version(p.get("version", null));

			PackageDescriptor desc = new PackageDescriptor(name, version, released);
			parsePackageBundles(desc, p.node(BUNDLE_REQS));
			return desc;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}

	}

	private void parsePackageBundles(PackageDescriptor pkg, Preferences bundleReqs) {
		String[] childrenNames = null;
		try {
			childrenNames = bundleReqs.childrenNames();
		} catch (BackingStoreException e) {
			return;
		}

		for (String id : childrenNames) {
			Preferences p = bundleReqs.node(id);
			String name = p.get("name", null);
			String lowVersion = p.get("low_version", null);
			String highVersion = p.get("high_version", null);

			VersionRange range = new VersionRange(new Version(lowVersion), new Version(highVersion));
			BundleRequirement bundleDesc = new BundleRequirement(name, range);
			pkg.getBundleRequirements().add(bundleDesc);
		}

	}

	public PackageDescriptor findInstalledPackage(String name) {
		Preferences packagesPrefs = getPackagePreferences().node("installed");
		try {
			if (!packagesPrefs.nodeExists(name))
				return null;
		} catch (BackingStoreException e) {
			return null;
		}

		return toPackageObject(name, packagesPrefs.node(name));
	}

	public void installPackage(PackageDescriptor pkg) throws AlreadyInstalledPackageException, BackingStoreException {
		Preferences packagesPrefs = getPackagePreferences().node("installed");

		// check already installed
		if (packagesPrefs.nodeExists(pkg.getName()))
			return;

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Preferences p = packagesPrefs.node(pkg.getName());
		p.put("version", pkg.getVersion().toString());
		p.put("released", dateFormat.format(pkg.getDate()));
		p.put("description", pkg.getDescription());

		Preferences bundleReqs = p.node(BUNDLE_REQS);
		int i = 1;
		for (BundleRequirement bundleDesc : pkg.getBundleRequirements()) {
			Preferences b = bundleReqs.node(Integer.toString(i++));
			b.put("name", bundleDesc.getName());
			b.put("low_version", bundleDesc.getVersionRange().getLow().toString());
			b.put("high_version", bundleDesc.getVersionRange().getHigh().toString());
		}

		sync(packagesPrefs);
	}

	public void uninstallPackage(String name) throws BackingStoreException {
		Preferences pkgs = getPackagePreferences().node("installed");
		if (!pkgs.nodeExists(name))
			return;

		Preferences p = pkgs.node(name);
		p.removeNode();
		sync(pkgs);
	}

	public void updatePackage(PackageDescriptor pkg) throws BackingStoreException {
		try {
			uninstallPackage(pkg.getName());
			installPackage(pkg);
		} catch (AlreadyInstalledPackageException e) {
			e.printStackTrace();
		}
	}

	private Preferences getPackagePreferences() {
		Preferences repos = prefs.node("/package");
		return repos;
	}
}
