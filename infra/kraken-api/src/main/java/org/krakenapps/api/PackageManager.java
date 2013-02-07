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
