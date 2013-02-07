/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.webconsole.plugins;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.krakenapps.api.BundleDescriptor;
import org.krakenapps.api.BundleRequirement;
import org.krakenapps.api.PackageDescriptor;
import org.krakenapps.api.PackageManager;
import org.krakenapps.api.PackageNotFoundException;
import org.krakenapps.api.PackageRepository;
import org.krakenapps.api.PackageUpdatePlan;
import org.krakenapps.api.PackageVersionHistory;
import org.krakenapps.api.ProgressMonitor;
import org.krakenapps.api.Version;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "webconsole-package-plugin")
@MsgbusPlugin
public class PackagePlugin {
	private final Logger logger = LoggerFactory.getLogger(PackagePlugin.class.getName());
	private PackageManager packageManager;

	public PackagePlugin(BundleContext bc) {
		ServiceReference ref = bc.getServiceReference(PackageManager.class.getName());
		if (ref != null)
			this.packageManager = (PackageManager) bc.getService(ref);
	}

	@MsgbusMethod
	public void getRepositories(Request req, Response resp) {
		List<Object> l = new ArrayList<Object>();

		for (PackageRepository r : packageManager.getRepositories()) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("alias", r.getAlias());
			m.put("url", r.getUrl());
			m.put("account", r.getAccount());
			m.put("trust_store", r.getTrustStoreAlias());
			m.put("key_store", r.getKeyStoreAlias());
			l.add(m);
		}

		resp.put("repositories", l);
	}

	@MsgbusMethod
	public void addRepository(Request req, Response resp) throws MalformedURLException {
		String alias = req.getString("alias");
		URL url = new URL(req.getString("url"));

		packageManager.addRepository(alias, url);
	}

	@MsgbusMethod
	public void addSecureRepository(Request req, Response resp) throws MalformedURLException {
		String alias = req.getString("alias");
		URL url = new URL(req.getString("url"));
		String trustStoreAlias = req.getString("trust_store");
		String keyStoreAlias = req.getString("key_store");

		packageManager.addSecureRepository(alias, url, trustStoreAlias, keyStoreAlias);
	}

	@MsgbusMethod
	public void removeRepository(Request req, Response resp) {
		String alias = req.getString("alias");
		packageManager.removeRepository(alias);
	}

	@MsgbusMethod
	public void getInstalledPackages(Request req, Response resp) {
		List<Object> l = new ArrayList<Object>();
		for (PackageDescriptor desc : packageManager.getInstalledPackages()) {
			l.add(marshal(desc));
		}
		resp.put("packages", l);
	}

	private Map<String, Object> marshal(PackageDescriptor desc) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", desc.getName());
		m.put("version", desc.getVersion().toString());
		m.put("date", dateFormat.format(desc.getDate()));
		m.put("description", desc.getDescription());
		return m;
	}

	@MsgbusMethod
	public void installPackage(Request req, Response resp) {
		String packageName = req.getString("name");
		String version = req.getString("version");
		ProgressMonitor monitor = newNullMonitor();

		try {
			packageManager.installPackage(packageName, version, monitor);
		} catch (Exception e) {
			logger.error("kraken webconsole: package install failed", e);
			throw new MsgbusException("core", "package-install-failed");
		}
	}

	@MsgbusMethod
	public void updatePackage(Request req, Response resp) {
		String packageName = req.getString("name");
		Version version = new Version(req.getString("version"));
		ProgressMonitor monitor = newNullMonitor();

		try {
			packageManager.updatePackage(packageName, version, monitor);
		} catch (Exception e) {
			logger.error("kraken webconsole: package update failed", e);
			throw new MsgbusException("core", "package-update-failed");
		}
	}

	@MsgbusMethod
	public void uninstallPackage(Request req, Response resp) {
		String packageName = req.getString("name");
		ProgressMonitor monitor = newNullMonitor();

		try {
			packageManager.uninstallPackage(packageName, monitor);
		} catch (Exception e) {
			logger.error("kraken webconsole: package uninstall failed", e);
			throw new MsgbusException("core", "package-uninstall-failed");
		}
	}

	@MsgbusMethod
	public void checkUpdate(Request req, Response resp) {
		String packageName = req.getString("name");
		try {
			PackageVersionHistory history = packageManager.checkUpdate(packageName);
			sendPackageVersion(resp, history);
		} catch (Exception e) {
			logger.error("kraken webconsole: package check update failed", e);
			throw new MsgbusException("core", "package-update-check-failed");
		}
	}

	@MsgbusMethod
	public void getLatestVersion(Request req, Response resp) {
		String packageName = req.getString("name");
		try {
			PackageVersionHistory history = packageManager.getLatestVersion(packageName);
			sendPackageVersion(resp, history);
		} catch (Exception e) {
			logger.error("kraken webconsole: package query failed", e);
			throw new MsgbusException("core", "package-update-check-failed");
		}
	}

	private void sendPackageVersion(Response resp, PackageVersionHistory history) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("updated", history.getLastUpdated());
		m.put("version", history.getVersion().toString());
		resp.put("latest", m);
	}

	@MsgbusMethod
	public void checkUninstallDependency(Request req, Response resp) {
		String packageName = req.getString("name");
		try {
			Map<String, List<PackageDescriptor>> plan = packageManager.checkUninstallDependency(packageName);
			Map<String, Object> dependency = marshalDependencies(plan);
			resp.put("dependency", dependency);
		} catch (PackageNotFoundException e) {
			throw new MsgbusException("core", "package-not-found");
		}
	}

	private Map<String, Object> marshalDependencies(Map<String, List<PackageDescriptor>> dependency) {
		Map<String, Object> m = new HashMap<String, Object>();
		for (String bundleSymbolicName : dependency.keySet()) {
			List<Object> l = new ArrayList<Object>();
			List<PackageDescriptor> descs = dependency.get(bundleSymbolicName);
			for (PackageDescriptor desc : descs)
				l.add(desc.getName());

			m.put(bundleSymbolicName, l);
		}

		return m;
	}

	@MsgbusMethod
	public void getUpdatePlan(Request req, Response resp) {
		String packageName = req.getString("name");
		Version version = new Version(req.getString("version"));

		try {
			PackageUpdatePlan plan = packageManager.getUpdatePlan(packageName, version);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("installing", marshalBundleRequirements(plan.getInstallingBundles()));
			m.put("remaining", marshalBundleDescriptors(plan.getRemainingBundles()));
			m.put("removing", marshalBundleDescriptors(plan.getRemovingBundles()));

			resp.put("plan", m);
		} catch (Exception e) {
			throw new MsgbusException("core", "package-check-update-dependency-failed");
		}
	}

	private List<Object> marshalBundleRequirements(Set<BundleRequirement> requirements) {
		List<Object> l = new ArrayList<Object>();

		for (BundleRequirement r : requirements) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("name", r.getName());
			m.put("version_from", r.getVersionRange().getLow().toString());
			m.put("version_to", r.getVersionRange().getHigh().toString());
			l.add(m);
		}

		return l;
	}

	private List<Object> marshalBundleDescriptors(Set<BundleDescriptor> descriptors) {
		List<Object> l = new ArrayList<Object>();

		for (BundleDescriptor d : descriptors) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("bundle_id", d.getBundleId());
			m.put("symbolic_name", d.getSymbolicName());
			m.put("version", d.getVersion());
			l.add(m);
		}

		return l;
	}

	private ProgressMonitor newNullMonitor() {
		ProgressMonitor monitor = new ProgressMonitor() {
			@Override
			public void writeln(String message) {
			}

			@Override
			public void write(String message) {
			}
		};
		return monitor;
	}

}
