package org.krakenapps.api;

import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PackageManager {
	List<PackageRepository> getRepositories();

	PackageRepository getRepository(String alias);

	@Deprecated
	void addRepository(String alias, URL url);

	@Deprecated
	void addSecureRepository(String alias, URL url, String trustStoreAlias, String keyStoreAlias);

	void createRepository(PackageRepository repo);

	void updateRepository(PackageRepository repo);

	void removeRepository(String alias);

	List<PackageDescriptor> getInstalledPackages();

	void installPackage(String packageName, String version, ProgressMonitor monitor) throws AlreadyInstalledPackageException,
			PackageNotFoundException, MavenResolveException, KeyStoreException, UnrecoverableKeyException, KeyManagementException;

	void updatePackage(String packageName, Version version, ProgressMonitor monitor) throws PackageNotFoundException,
			MavenResolveException, KeyStoreException, UnrecoverableKeyException, KeyManagementException;

	void uninstallPackage(String packageName, ProgressMonitor monitor) throws PackageNotFoundException;

	Set<BundleDescriptor> findRelatedBundles(PackageDescriptor pkg);

	Map<String, List<PackageDescriptor>> checkUninstallDependency(String packageName) throws PackageNotFoundException;

	PackageUpdatePlan getUpdatePlan(String packageName, Version version) throws PackageNotFoundException, KeyStoreException,
			UnrecoverableKeyException, KeyManagementException;

	PackageVersionHistory checkUpdate(String packageName) throws PackageNotFoundException, KeyStoreException,
			UnrecoverableKeyException, KeyManagementException;

	PackageVersionHistory getLatestVersion(String packageName) throws PackageNotFoundException, KeyStoreException,
			UnrecoverableKeyException, KeyManagementException;

	PackageDescriptor findInstalledPackage(String packageName);

	List<PackageIndex> getPackageIndexes();

	PackageIndex getPackageIndex(PackageRepository repo);
}
