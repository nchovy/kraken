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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.httpclient.HttpException;
import org.krakenapps.api.AlreadyInstalledPackageException;
import org.krakenapps.api.BundleDescriptor;
import org.krakenapps.api.BundleManager;
import org.krakenapps.api.BundleRepository;
import org.krakenapps.api.BundleRequirement;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.api.MavenArtifact;
import org.krakenapps.api.MavenResolveException;
import org.krakenapps.api.PackageDescriptor;
import org.krakenapps.api.PackageIndex;
import org.krakenapps.api.PackageManager;
import org.krakenapps.api.PackageMetadata;
import org.krakenapps.api.PackageNotFoundException;
import org.krakenapps.api.PackageRepository;
import org.krakenapps.api.PackageUpdatePlan;
import org.krakenapps.api.PackageVersionHistory;
import org.krakenapps.api.ProgressMonitor;
import org.krakenapps.api.Version;
import org.krakenapps.api.VersionRange;
import org.krakenapps.confdb.ConfigService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageManagerService implements PackageManager {
	private final Logger logger = LoggerFactory.getLogger(PackageManagerService.class.getName());

	private PackageDatabase db;
	private BundleContext bc;
	private BundleManager bundleManager;

	public PackageManagerService(BundleContext bc) {
		this.bc = bc;
		ServiceReference ref = bc.getServiceReference(BundleManager.class.getName());
		bundleManager = (BundleManager) bc.getService(ref);
		db = new PackageDatabase(getConfigService());
	}

	@Override
	public List<PackageDescriptor> getInstalledPackages() {
		return db.getInstalledPackages();
	}

	@Override
	public void updatePackage(String packageName, Version version, ProgressMonitor monitor) throws PackageNotFoundException,
			MavenResolveException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {
		PackageUpdatePlan dep = getUpdatePlan(packageName, version);

		PackageMetadata metadata = downloadMetadata(packageName);
		PackageVersionHistory choice = findPackageVersionHistory(version, metadata);
		PackageDescriptor newPkg = downloadPackageDesc(metadata, choice);

		if (dep.getRemovingBundles().size() > 0)
			monitor.writeln("Removing Bundles");

		for (BundleDescriptor bundle : dep.getRemovingBundles()) {
			monitor.writeln("  -> Removing: " + bundle.getSymbolicName() + " " + bundle.getVersion());
			bundleManager.uninstallBundle(bundle.getBundleId());
		}

		monitor.writeln("Installing Bundles");

		try {
			installMavenArtifacts(metadata, newPkg, monitor);

			// force bundle refresh
			bundleManager.refresh();

			startBundles(newPkg, monitor);

			db.updatePackage(newPkg);
		} catch (BundleException e) {
			throw new RuntimeException(e);
		}
	}

	public void installPackage(String packageName, String version, ProgressMonitor monitor)
			throws AlreadyInstalledPackageException, PackageNotFoundException, MavenResolveException, KeyStoreException,
			UnrecoverableKeyException, KeyManagementException {
		PackageDescriptor pkg = db.findInstalledPackage(packageName);
		if (pkg != null)
			throw new AlreadyInstalledPackageException(pkg);

		PackageMetadata metadata = downloadMetadata(packageName);
		if (metadata == null)
			throw new PackageNotFoundException(packageName);

		PackageVersionHistory ver = selectVersion(version, metadata);
		if (ver == null)
			throw new PackageNotFoundException(packageName);

		PackageDescriptor newPkg = downloadPackageDesc(metadata, ver);

		// download description and download maven artifacts
		logger.info("package manager: downloading {}", newPkg);

		try {
			// download maven bundles and inspect bundle metadata
			installMavenArtifacts(metadata, newPkg, monitor);

			// force bundle refresh
			bundleManager.refresh();

			// start bundles
			startBundles(newPkg, monitor);

			db.installPackage(newPkg);
		} catch (BundleException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<PackageIndex> getPackageIndexes() {
		List<PackageIndex> list = new ArrayList<PackageIndex>();
		for (PackageRepository repo : db.getPackageRepositories()) {
			try {
				list.add(getPackageIndex(repo));
			} catch (Throwable t) {
				logger.error("kraken core: cannot download package index", t);
			}
		}
		return list;
	}

	@Override
	public PackageIndex getPackageIndex(PackageRepository repo) {
		try {
			URL url = new URL(normalize(repo.getUrl()) + "kraken-package.index");
			byte[] body = download(repo, url);
			return PackageIndexParser.parse(repo, body);
		} catch (Throwable t) {
			throw new IllegalStateException("cannot download package index from " + repo.getUrl(), t);
		}
	}

	private PackageVersionHistory selectVersion(String version, PackageMetadata metadata) {
		if (version == null)
			return metadata.getVersions().get(0);

		Version v = new Version(version);
		for (PackageVersionHistory h : metadata.getVersions())
			if (h.getVersion().equals(v))
				return h;

		return null;
	}

	private void startBundles(PackageDescriptor pkg, ProgressMonitor monitor) {
		monitor.writeln("Starting Bundles");
		Map<String, Bundle> bundleMap = new HashMap<String, Bundle>();

		for (Bundle bundle : bc.getBundles()) {
			bundleMap.put(bundle.getSymbolicName(), bundle);
		}

		// start bundles in order
		for (String symbolicName : pkg.getStartBundleNames()) {
			Bundle bundle = bundleMap.get(symbolicName);
			if (bundle == null) {
				monitor.writeln(String.format("  -> [FAIL] %s not found", symbolicName));
				continue;
			}

			if (bundle.getState() == Bundle.INSTALLED || bundle.getState() == Bundle.RESOLVED) {
				try {
					bundle.start();
					monitor.writeln(String.format("  -> [OK] %s %s", bundle.getSymbolicName(), bundle.getVersion()));
				} catch (BundleException e) {
					monitor.writeln(String.format("  -> [FAIL] %s %s: %s", bundle.getSymbolicName(), bundle.getVersion(),
							e.getMessage()));
				}
			}
		}
	}

	private void installMavenArtifacts(PackageMetadata metadata, PackageDescriptor newPkg, ProgressMonitor monitor)
			throws MavenResolveException, BundleException {
		// prepare repository set
		Set<String> urls = metadata.getMavenRepositories();
		List<BundleRepository> configs = bundleManager.getRepositories();

		List<BundleRepository> remotes = new ArrayList<BundleRepository>();
		for (BundleRepository c : configs) {
			URL url = c.getUrl();
			if (urls.contains(url))
				urls.remove(url);

			remotes.add(c);
		}

		for (String url : urls) {
			try {
				remotes.add(new BundleRepository("", new URL(url)));
			} catch (MalformedURLException e) {
			}
		}

		File localRepository = new File(getDownloadRoot());
		KeyStoreManager keyStoreManager = getKeyStoreManager();

		for (MavenArtifact artifact : newPkg.getMavenArtifacts()) {
			monitor.writeln("Resolving " + artifact);
			File file = new MavenResolver(localRepository, remotes, monitor, keyStoreManager).resolve(artifact);
			monitor.writeln("  -> resolved");

			String bundlePath = null;
			if (File.separatorChar == '\\')
				bundlePath = "file:///" + file.getAbsolutePath().replace('\\', '/');
			else
				bundlePath = "file://" + file.getAbsolutePath();

			if (isInstallRequired(file, newPkg)) {
				String symbolicName = getBundleSymbolicName(file);
				Version version = getBundleVersion(file);
				monitor.writeln("  -> installing: " + symbolicName + " " + version);
				bc.installBundle(bundlePath);
			}

			monitor.writeln("");
		}
	}

	private String getDownloadRoot() {
		return new File(System.getProperty("kraken.download.dir")).getAbsolutePath();
	}

	private String getBundleSymbolicName(File file) {
		JarFile jar = null;
		try {
			jar = new JarFile(file);
			Attributes attrs = jar.getManifest().getMainAttributes();
			// metadata can be added followed by semicolon (e.g. ;singleton)
			return attrs.getValue("Bundle-SymbolicName").split(";")[0].trim();
		} catch (IOException e) {
			logger.error("package manager: symbolic name not found", e);
			return null;
		} finally {
			if (jar != null)
				try {
					jar.close();
				} catch (IOException e) {
				}
		}
	}

	private Version getBundleVersion(File file) {
		JarFile jar = null;
		try {
			jar = new JarFile(file);
			Attributes attrs = jar.getManifest().getMainAttributes();
			return new Version(attrs.getValue("Bundle-Version"));
		} catch (IOException e) {
			logger.error("package manager: bundle version not found", e);
			return null;
		} finally {
			if (jar != null)
				try {
					jar.close();
				} catch (IOException e) {
				}
		}
	}

	private boolean isInstallRequired(File file, PackageDescriptor packageDesc) {
		String bundleSymbolicName = getBundleSymbolicName(file);
		Version bundleVersion = getBundleVersion(file);

		// find related bundle requirement
		VersionRange requiredRange = null;
		for (BundleRequirement bundleDesc : packageDesc.getBundleRequirements()) {
			if (bundleSymbolicName.equals(bundleDesc.getName())) {
				requiredRange = bundleDesc.getVersionRange();
				break;
			}
		}

		if (requiredRange == null) {
			String log = "package manager: cannot find version requirement for {}, check package syntax.";
			logger.warn(log, bundleSymbolicName);
			return false;
		}

		for (Bundle bundle : bc.getBundles()) {
			if (!bundle.getSymbolicName().equals(bundleSymbolicName))
				continue;

			// found bundle, but is it satisfy requirement?
			Version current = new Version(bundle.getVersion().toString());
			if (requiredRange.contains(current)) {
				logger.info("package manager: no install required - {} {}", bundle.getSymbolicName(), current);
				return false;
			}

		}

		// verify new bundle version
		if (!requiredRange.contains(bundleVersion)) {
			logger.error("package manager: new version also cannot satisfy requirement: {}", bundleVersion);
			return false;
		}

		return true;
	}

	@Override
	public Map<String, List<PackageDescriptor>> checkUninstallDependency(String packageName) throws PackageNotFoundException {
		PackageDescriptor pkg = getInstalledPackage(packageName);

		Set<BundleDescriptor> relatedBundles = findRelatedBundles(pkg);
		List<PackageDescriptor> packages = db.getInstalledPackages();

		// bundle -> package desc list
		Map<String, List<PackageDescriptor>> dependMap = new HashMap<String, List<PackageDescriptor>>();

		for (BundleDescriptor bundle : relatedBundles) {
			for (PackageDescriptor desc : packages) {
				// skip uninstall target package
				if (desc.getName().equals(packageName))
					continue;

				Version version = new Version(bundle.getVersion().toString());
				if (isUsedByPackage(bundle.getSymbolicName(), version, desc)) {
					List<PackageDescriptor> l = dependMap.get(bundle.getSymbolicName());
					if (l == null) {
						l = new ArrayList<PackageDescriptor>();
						dependMap.put(bundle.getSymbolicName(), l);
					}

					l.add(desc);
				}
			}
		}

		return dependMap;
	}

	@Override
	public Set<BundleDescriptor> findRelatedBundles(PackageDescriptor pkg) {
		Map<String, BundleRequirement> reqMap = new HashMap<String, BundleRequirement>();
		for (BundleRequirement req : pkg.getBundleRequirements()) {
			reqMap.put(req.getName(), req);
		}

		Set<BundleDescriptor> relatedBundles = new HashSet<BundleDescriptor>();
		for (Bundle bundle : bc.getBundles()) {
			BundleRequirement req = reqMap.get(bundle.getSymbolicName());
			if (req == null)
				continue;

			if (req.getVersionRange().contains(new Version(bundle.getVersion().toString())))
				relatedBundles.add(new BundleDescriptor(bundle.getBundleId(), bundle.getSymbolicName(), bundle.getVersion()
						.toString()));
		}

		return relatedBundles;
	}

	private boolean isUsedByPackage(String name, Version version, PackageDescriptor pkg) {
		for (BundleRequirement req : pkg.getBundleRequirements()) {
			if (!name.equals(req.getName()))
				continue;

			if (req.getVersionRange().contains(version))
				return true;
		}

		return false;
	}

	@Override
	public PackageUpdatePlan getUpdatePlan(String packageName, Version version) throws PackageNotFoundException,
			KeyStoreException, UnrecoverableKeyException, KeyManagementException {
		PackageUpdatePlan dep = new PackageUpdatePlan();

		PackageMetadata metadata = downloadMetadata(packageName);
		if (metadata == null)
			throw new PackageNotFoundException(packageName);

		PackageVersionHistory choice = findPackageVersionHistory(version, metadata);

		PackageDescriptor newPkg = downloadPackageDesc(metadata, choice);
		PackageDescriptor oldPkg = getInstalledPackage(packageName);
		Set<BundleDescriptor> bundles = findRelatedBundles(oldPkg);

		// add all to removing bundles first
		for (BundleDescriptor bundle : bundles)
			if (isUsedByPackage(bundle, oldPkg.getName()))
				dep.getRemainingBundles().add(bundle);
			else
				dep.getRemovingBundles().add(bundle);

		// find install required bundles
		for (BundleRequirement req : newPkg.getBundleRequirements()) {
			BundleDescriptor bundle = findMatchedBundle(req, bundles);
			if (bundle != null) {
				dep.getRemovingBundles().remove(bundle);
				dep.getRemainingBundles().add(bundle);
			} else {
				dep.getInstallingBundles().add(req);
			}
		}

		return dep;
	}

	private boolean isUsedByPackage(BundleDescriptor bundle, String exceptPackageName) {
		for (PackageDescriptor pkg : getInstalledPackages()) {
			if (pkg.getName().equals(exceptPackageName))
				continue;

			String name = bundle.getSymbolicName();
			Version version = new Version(bundle.getVersion().toString());
			if (isUsedByPackage(name, version, pkg))
				return true;
		}

		return false;
	}

	private PackageVersionHistory findPackageVersionHistory(Version version, PackageMetadata metadata) {
		PackageVersionHistory choice = null;

		for (PackageVersionHistory it : metadata.getVersions())
			if (it.getVersion().equals(version))
				choice = it;
		return choice;
	}

	private BundleDescriptor findMatchedBundle(BundleRequirement req, Set<BundleDescriptor> bundles) {
		for (BundleDescriptor bundle : bundles) {
			if (req.getName().equals(bundle.getSymbolicName())
					&& req.getVersionRange().contains(new Version(bundle.getVersion().toString())))
				return bundle;
		}

		return null;
	}

	@Override
	public void uninstallPackage(String packageName, ProgressMonitor monitor) throws PackageNotFoundException {
		PackageDescriptor pkg = findInstalledPackage(packageName);
		Map<String, List<PackageDescriptor>> dependMap = checkUninstallDependency(packageName);

		for (BundleDescriptor bundle : findRelatedBundles(pkg)) {
			if (!dependMap.containsKey(bundle.getSymbolicName())) {
				monitor.writeln("  Removing: " + bundle.getSymbolicName() + " " + bundle.getVersion());
				bundleManager.uninstallBundle(bundle.getBundleId());
			}
		}

		db.uninstallPackage(pkg.getName());
	}

	@Override
	public List<PackageRepository> getRepositories() {
		return db.getPackageRepositories();
	}

	@Override
	public PackageRepository getRepository(String alias) {
		return db.getPackageRepository(alias);
	}

	@Override
	public void createRepository(PackageRepository repo) {
		db.createPackageRepository(repo);
	}

	@Override
	public void updateRepository(PackageRepository repo) {
		db.updatePackageRepository(repo);
	}

	@Override
	public void addRepository(String alias, URL url) {
		createRepository(PackageRepository.create(alias, url));
	}

	@Override
	public void addSecureRepository(String alias, URL url, String trustStoreAlias, String keyStoreAlias) {
		createRepository(PackageRepository.createHttps(alias, url, trustStoreAlias, keyStoreAlias));
	}

	@Override
	public void removeRepository(String alias) {
		db.removePackageRepository(alias);
	}

	private PackageDescriptor getInstalledPackage(String packageName) throws PackageNotFoundException {
		PackageDescriptor installedPackage = db.findInstalledPackage(packageName);
		if (installedPackage == null)
			throw new PackageNotFoundException(packageName);

		return installedPackage;
	}

	private PackageDescriptor downloadPackageDesc(PackageMetadata metadata, PackageVersionHistory history)
			throws PackageNotFoundException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {
		PackageRepository repository = metadata.getRepository();
		String pacakgeName = metadata.getName();
		Version version = history.getVersion();

		try {
			URL url = new URL(normalize(repository.getUrl()) + pacakgeName + "/" + version + "/kraken.package");
			String body = downloadString(repository, url);
			return PackageDescParser.parse(metadata, history, body);
		} catch (HttpException e) {
			logger.error("package manager: http exception", e);
		} catch (IOException e) {
			logger.error("package manager: io exception", e);
		}

		throw new PackageNotFoundException(metadata.getName());
	}

	private PackageMetadata downloadMetadata(String packageName) throws KeyStoreException, UnrecoverableKeyException,
			KeyManagementException {
		List<PackageRepository> repositories = getRepositories();
		PackageMetadata metadata = null;

		for (PackageRepository repo : repositories) {
			metadata = downloadMetadata(repo, packageName);
			if (metadata != null)
				break;
		}

		return metadata;
	}

	private PackageMetadata downloadMetadata(PackageRepository repository, String packageName) throws KeyStoreException,
			UnrecoverableKeyException, KeyManagementException {
		try {
			URL url = new URL(normalize(repository.getUrl()) + packageName + "/kraken.package");
			String body = downloadString(repository, url);
			PackageMetadata metadata = PackageMetadataParser.parse(body);
			metadata.setName(packageName);
			metadata.setRepository(repository);
			return metadata;
		} catch (IOException e) {
			return null;
		}
	}

	private byte[] download(PackageRepository repo, URL url) throws IOException, KeyStoreException, UnrecoverableKeyException,
			KeyManagementException {
		if (repo.isLocalFilesystem()) {
			try {
				File file = new File(url.toURI());
				long length = file.length();
				FileInputStream stream = new FileInputStream(file);
				byte[] b = new byte[(int) length];
				stream.read(b);
				return b;
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return new byte[0];
			}
		} else if (repo.isHttps()) {
			ServiceReference ref = bc.getServiceReference(KeyStoreManager.class.getName());
			KeyStoreManager keyman = (KeyStoreManager) bc.getService(ref);
			try {
				TrustManagerFactory tmf = keyman.getTrustManagerFactory(repo.getTrustStoreAlias(), "SunX509");
				KeyManagerFactory kmf = keyman.getKeyManagerFactory(repo.getKeyStoreAlias(), "SunX509");
				HttpWagon.download(url, tmf, kmf);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}

			return HttpWagon.download(url);
		} else if (repo.isAuthRequired())
			return HttpWagon.download(url, true, repo.getAccount(), repo.getPassword());
		return HttpWagon.download(url);
	}

	private String downloadString(PackageRepository repo, URL url) throws IOException, KeyStoreException,
			UnrecoverableKeyException, KeyManagementException {
		if (repo.isLocalFilesystem()) {
			try {
				File file = new File(url.toURI());
				long length = file.length();
				FileInputStream stream = new FileInputStream(file);
				byte[] b = new byte[(int) length];
				stream.read(b);
				return new String(b, Charset.forName("UTF-8"));
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return new String();
			}
		} else if (repo.isHttps()) {
			ServiceReference ref = bc.getServiceReference(KeyStoreManager.class.getName());
			KeyStoreManager keyman = (KeyStoreManager) bc.getService(ref);
			try {
				TrustManagerFactory tmf = keyman.getTrustManagerFactory(repo.getTrustStoreAlias(), "SunX509");
				KeyManagerFactory kmf = keyman.getKeyManagerFactory(repo.getKeyStoreAlias(), "SunX509");
				HttpWagon.download(url, tmf, kmf);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}

			return HttpWagon.downloadString(url);
		} else if (repo.isAuthRequired())
			return HttpWagon.downloadString(url, repo.getAccount(), repo.getPassword());
		return HttpWagon.downloadString(url);
	}

	private String normalize(URL repository) {
		String repoUrl = repository.toString();
		char lastChar = repoUrl.charAt(repoUrl.length() - 1);
		if (lastChar != '/')
			return repoUrl + "/";

		return repoUrl;
	}

	@Override
	public PackageVersionHistory getLatestVersion(String packageName) throws PackageNotFoundException, KeyStoreException,
			UnrecoverableKeyException, KeyManagementException {
		PackageMetadata metadata = downloadMetadata(packageName);
		if (metadata == null)
			throw new PackageNotFoundException(packageName);

		return metadata.getVersions().get(0);
	}

	@Override
	public PackageVersionHistory checkUpdate(String packageName) throws PackageNotFoundException, KeyStoreException,
			UnrecoverableKeyException, KeyManagementException {
		PackageMetadata metadata = downloadMetadata(packageName);
		if (metadata == null)
			throw new PackageNotFoundException(packageName);

		PackageVersionHistory history = metadata.getVersions().get(0);
		PackageDescriptor installedPackage = getInstalledPackage(packageName);

		if (needUpdate(installedPackage, history))
			return history;

		return null;
	}

	private boolean needUpdate(PackageDescriptor installedPackage, PackageVersionHistory history) {
		int versionDiff = history.getVersion().compareTo(installedPackage.getVersion());

		Date currentDate = installedPackage.getDate();
		Date latestDate = history.getLastUpdated();

		if (versionDiff > 0 || (versionDiff == 0 && currentDate.compareTo(latestDate) < 0))
			return true;

		if (versionDiff == 0 && currentDate.compareTo(latestDate) > 0) {
			logger.warn("repository version is older. curious.");
		}

		return false;
	}

	@Override
	public PackageDescriptor findInstalledPackage(String name) {
		return db.findInstalledPackage(name);
	}

	private ConfigService getConfigService() {
		ServiceReference ref = bc.getServiceReference(ConfigService.class.getName());
		return (ConfigService) bc.getService(ref);
	}

	private KeyStoreManager getKeyStoreManager() {
		ServiceReference ref = bc.getServiceReference(KeyStoreManager.class.getName());
		KeyStoreManager keyman = (KeyStoreManager) bc.getService(ref);
		return keyman;
	}
}