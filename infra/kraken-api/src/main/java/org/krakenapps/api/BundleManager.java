package org.krakenapps.api;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public interface BundleManager {

	/**
	 * Clean all out-dated versions and re-calculate bundle dependencies using
	 * PackageAdmin.
	 * 
	 * @see org.osgi.service.packageadmin.PackageAdmin in OSGi compendium
	 *      specification.
	 */
	void refresh();

	/**
	 * Fetch all registered maven repositories
	 * 
	 * @return the remote maven repositories
	 */
	@Deprecated
	List<BundleRepository> getRemoteRepositories();
	
	List<BundleRepository> getRepositories();
	
	BundleRepository getRepository(String alias);

	void addRepository(BundleRepository repo);

	void updateRepository(BundleRepository repo);

	void removeRepository(String alias);

	/**
	 * Add maven repository configuration.
	 * 
	 * @param alias
	 *            the repository alias
	 * @param url
	 *            the repository url
	 * @throws BackingStoreException
	 */
	@Deprecated
	void addRemoteRepository(String alias, URL url);

	@Deprecated
	void addSecureRemoteRepository(String alias, URL url, String trustStoreAlias, String keyStoreAlias);

	/**
	 * Remove maven repository configuration.
	 * 
	 * @param alias
	 *            the repository alias that you want to remove
	 * @return the removed repository url
	 * @throws BackingStoreException
	 */
	@Deprecated
	void removeRemoteRepository(String alias);

	/**
	 * Install new OSGi bundle from file system.
	 * 
	 * @param filePath
	 *            the OSGi bundle path
	 * @return the installed OSGi bundle id
	 * @throws Exception
	 */
	long installBundle(String filePath);

	/**
	 * Install new OSGi bundle from maven repository.
	 * 
	 * @param monitor
	 *            the monitor for tracking install states
	 * @param groupId
	 *            the maven bundle group id
	 * @param artifactId
	 *            the maven bundle artifact id
	 * @param version
	 *            the maven bundle version
	 * @return the installed OSGi bundle id
	 * @throws BundleException
	 * @throws MavenResolveException
	 * @throws BackingStoreException
	 * @throws Exception
	 */
	long installBundle(ProgressMonitor monitor, String groupId, String artifactId, String version) throws MavenResolveException;

	boolean uninstallBundle(long bundleId);

	/**
	 * Start the bundle.
	 * 
	 * @param bundleId
	 *            the bundle id
	 * @throws BundleException
	 */
	void startBundle(long bundleId);

	/**
	 * Stop the bundle.
	 * 
	 * @param bundleId
	 *            the bundle id
	 * @throws BundleException
	 */
	void stopBundle(long bundleId);

	/**
	 * Update the bundle. Refresh by PackageAdmin may be needed.
	 * 
	 * @param bundleId
	 *            the bundle id
	 * @throws BundleException
	 */
	void updateBundle(long bundleId);

	Map<Long, BundleStatus> getBundles();

	/**
	 * Get bundle installation path. It is used again when you update bundle.
	 * 
	 * @param bundleId
	 *            the bundle id
	 * @return the installation path of specified bundle
	 * @throws BundleException
	 */
	String getBundleLocation(long bundleId);

	/**
	 * Grab content of specified entry. But only utf-8 encoding supported now.
	 * 
	 * @param bundleId
	 *            the bundle id for browsing
	 * @param path
	 *            the absolute path of entry (without scheme)
	 * @return the content of entry
	 * @throws BundleException
	 * @throws IOException
	 */
	String getEntry(long bundleId, String path) throws IOException;

	/**
	 * Browse all entries in the specified directory in the bundle.
	 * 
	 * @param bundleId
	 *            the bundle id for browsing
	 * @param directory
	 *            the directory path
	 * @return the all entry paths in specified directory of specified bundle
	 * @throws BundleException
	 */
	List<String> getEntryPaths(long bundleId, String directory);

	boolean isLocallyInstalledBundle(long bundleId);
}