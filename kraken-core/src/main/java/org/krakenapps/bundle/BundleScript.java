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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.krakenapps.api.BundleManager;
import org.krakenapps.api.BundleRepository;
import org.krakenapps.api.BundleStatus;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.api.MavenResolveException;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.main.Kraken;
import org.krakenapps.pkg.MavenMetadata;
import org.krakenapps.pkg.ProgressMonitorImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;

public class BundleScript implements Script {
	private static final String BUNDLE_REPO_PATH = "/bundle/repo";
	private ScriptContext context;
	private BundleManager manager;

	public BundleScript(BundleManager manager) {
		this.manager = manager;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void refresh(String[] args) {
		manager.refresh();
		context.println("bundles are refreshed.");
	}

	private static class PriorityComparator implements Comparator<BundleRepository> {
		@Override
		public int compare(BundleRepository o1, BundleRepository o2) {
			return o2.getPriority() - o1.getPriority();
		}
	}

	public void downloadroot(String[] args) {
		String path = new File(System.getProperty("kraken.download.dir")).getAbsolutePath().replaceAll("\\\\", "/");
		context.println(path);
	}

	public void repositories(String[] args) {
		try {
			List<BundleRepository> repositories = manager.getRemoteRepositories();
			context.println("Maven Bundle Repository");
			drawLine(70);
			Collections.sort(repositories, new PriorityComparator());
			for (BundleRepository repo : repositories) {
				String name = repo.getName();
				if (repo.isAuthRequired())
					name += " (http-auth)";

				if (repo.isHttps())
					context.printf("[(%3d) %s] %s, trust=%s, key=%s\n", repo.getPriority(), name, repo.getUrl(),
							repo.getTrustStoreAlias(), repo.getKeyStoreAlias());
				else
					context.printf("[(%3d) %s] %s\n", repo.getPriority(), name, repo.getUrl());
			}
		} catch (Exception e) {
			context.println("error: " + e.getMessage());
		}
	}

	@ScriptUsage(description = "Add maven repository", arguments = {
			@ScriptArgument(name = "alias", type = "string", description = "alias of the maven repository"),
			@ScriptArgument(name = "url", type = "string", description = "url of the maven repository") })
	public void addRepository(String[] args) {
		try {
			manager.addRemoteRepository(args[0], new URL(args[1]));
			context.println(args[1] + " added.");
		} catch (Exception e) {
			context.println("error: " + e.getMessage());
		}
	}

	@ScriptUsage(description = "add secure bundle repository", arguments = {
			@ScriptArgument(name = "alias", type = "string", description = "the alias of bundle repository"),
			@ScriptArgument(name = "url", type = "string", description = "the url of bundle repository"),
			@ScriptArgument(name = "trust store alias", type = "string", description = "the alias of truststore"),
			@ScriptArgument(name = "key store alias", type = "string",
					description = "the alias of keystore. if provided, client authentication will be used",
					optional = true) })
	public void addSecureRepository(String[] args) {
		try {
			String alias = args[0];
			URL url = new URL(args[1]);
			String trustStoreAlias = args[2];
			String keyStoreAlias = null;
			if (args.length >= 4)
				keyStoreAlias = args[3];

			manager.addSecureRemoteRepository(alias, url, trustStoreAlias, keyStoreAlias);
			context.printf("secure repository [%s] added\n", alias);
		} catch (MalformedURLException e) {
			context.println("invalid url format");
		} catch (IllegalStateException e) {
			context.println("database failure");
		}

	}

	@ScriptUsage(description = "Remove maven repository", arguments = { @ScriptArgument(name = "alias",
			type = "string", description = "alias of the maven repository") })
	public void removeRepository(String[] args) {
		try {
			String alias = args[0];
			manager.removeRemoteRepository(alias);
			context.println(alias + " removed.");
		} catch (Exception e) {
			context.println("error: " + e.getMessage());
		}
	}

	@ScriptUsage(description = "Set credential for repository http authentication", arguments = {
			@ScriptArgument(name = "alias", type = "string", description = "alias of the maven repository"),
			@ScriptArgument(name = "account", type = "string", description = "account for http authentication"),
			@ScriptArgument(name = "password", type = "string", description = "password for http authentication") })
	public void setHttpAuth(String[] args) {
		Preferences prefs = getRepositoryPreferences(Kraken.getContext());
		String alias = args[0];
		String account = args[1];
		String password = args[2];
		Preferences repo = prefs.node(alias);
		repo.put("account", account);
		repo.put("password", password);
		repo.putBoolean("auth", true);
		sync(repo);
	}

	@ScriptUsage(description = "Reset credential for repository http authentication", arguments = { @ScriptArgument(
			name = "alias", type = "string", description = "alias of the maven repository") })
	public void resetHttpAuth(String[] args) {
		Preferences prefs = getRepositoryPreferences(Kraken.getContext());
		String alias = args[0];

		Preferences repo = prefs.node(alias);
		repo.remove("account");
		repo.remove("password");
		repo.remove("auth");
		sync(repo);
	}

	@ScriptUsage(description = "Set priority of repository. Larger number means higher priority", arguments = {
			@ScriptArgument(name = "alias", type = "string", description = "alias of the maven repository"),
			@ScriptArgument(name = "new priority", type = "integer", description = "new priority of the repository") })
	public void setRepositoryPriority(String[] args) {
		Preferences prefs = getRepositoryPreferences(Kraken.getContext());
		String alias = args[0];
		int newPriority = Integer.parseInt(args[1]);
		Preferences repo = prefs.node(alias);
		repo.putInt("priority", newPriority);
		sync(repo);
	}

	private void sync(Preferences repo) {
		try {
			repo.flush();
			repo.sync();
		} catch (BackingStoreException e) {
		}
	}

	private Preferences getRepositoryPreferences(BundleContext bc) {
		ServiceReference ref = bc.getServiceReference(PreferencesService.class.getName());
		PreferencesService prefsService = (PreferencesService) bc.getService(ref);
		Preferences systemPrefs = prefsService.getSystemPreferences();
		return systemPrefs.node(BUNDLE_REPO_PATH);
	}

	public void install(String[] args) {
		try {
			if (args.length == 0)
				throw new IllegalArgumentException();
			long bundleId = -1;
			if (args.length == 1) {
				if (args[0].startsWith("file://")) {
					bundleId = manager.installBundle(args[0]);
				} else if (args[0].startsWith("file:\\\\")) {
					String path = args[0].replace('\\', '/');
					bundleId = manager.installBundle(path);
				} else {
					context.println("local path should starts with file:// or file:\\\\");
					return;
				}
			} else {
				if (args.length > 3) {
					throw new IllegalArgumentException();
				}
				String groupId = args[0];
				String artifactId = args[1];
				String version = null;
				if (args.length == 3)
					version = args[2];

				bundleId = manager.installBundle(new ProgressMonitorImpl(context), groupId, artifactId, version);
			}

			context.printf("bundle [%d] loaded\n", bundleId);
		} catch (MavenResolveException e) {
			context.println("Artifact not found");
		} catch (IllegalStateException e) {
			context.println(e.getMessage());
		} catch (IllegalArgumentException e) {
			context.println("Usage:  bundle.install bundlePath ");
			context.println("        bundle.install groupId artifactId version");
			context.println("    bundlePath example: file:///C:\\bundle\\sample.jar or file:///root/kraken/sample.jar");
		}
	}

	@ScriptUsage(description = "restart the bundle(s)")
	public void restart(String[] args) {
		stop(args);
		start(args);
	}

	@ScriptUsage(description = "start the bundle(s)")
	public void start(String[] args) {
		long bundleId = -1;
		try {
			for (String arg : args) {
				bundleId = Long.parseLong(arg);
				manager.startBundle(bundleId);
				context.println("bundle " + bundleId + " started.");
			}
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "stop the bundle(s)")
	public void stop(String[] args) {
		long bundleId = -1;
		try {
			for (String arg : args) {
				bundleId = Long.parseLong(arg);
				manager.stopBundle(bundleId);
				context.println("bundle " + bundleId + " stopped.");
			}
		} catch (Exception e) {
			context.println("failed to stop bundle " + bundleId);
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "update the bundle(s)")
	public void update(String[] args) {
		try {
			for (String arg : args) {
				long bundleId = Long.parseLong(arg);
				manager.updateBundle(bundleId);
				context.println("bundle " + bundleId + " updated.");
			}
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(
			description = "update all locally-installed bundle(s). before use this method, stop ipojo bundle first.",
			arguments = { @ScriptArgument(name = "isForced", description = "use 'force' to run this method.",
					optional = false, type = "force or not") })
	public void updateAll(String[] args) {
		if (args.length < 1 || !args[0].equals("force")) {
			context.printf("Ignored. Use 'force' as argument.\n");
			return;
		}
		try {
			Map<Long, BundleStatus> bundles = manager.getBundles();
			for (Long bundleId : bundles.keySet()) {
				BundleStatus bundleStatus = bundles.get(bundleId);
				try {
					if (manager.isLocallyInstalledBundle(bundleId)) {
						manager.updateBundle(bundleId);
						context.printf("bundle [%d] %s %s updated.\n", bundleId, bundleStatus.getSymbolicName(),
								bundleStatus.getVersion());
					}
				} catch (Exception be) {
					continue;
				}
			}
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "uninstall the bundle(s)")
	public void uninstall(String[] args) {
		for (String arg : args) {
			long bundleId = Long.parseLong(arg);
			if (manager.uninstallBundle(bundleId))
				context.println("bundle " + bundleId + " uninstalled successfully.");
			else
				context.println("bundle " + bundleId + " uninstall failed.");
		}
	}

	@ScriptUsage(description = "list all bundles", arguments = { @ScriptArgument(name = "filter", type = "string",
			description = "filter text for bundle symbolic name", optional = true) })
	public void list(String[] args) {
		String filterText = null;
		if (args.length > 0)
			filterText = args[0];

		Map<Long, BundleStatus> bundles = manager.getBundles();
		context.println("[ ID] Symbolic Name\t\t\t\tVersion\t  Status");
		drawLine(66);

		Set<Long> sortedKeys = new TreeSet<Long>(bundles.keySet());
		for (Long bundleId : sortedKeys) {
			BundleStatus status = bundles.get(bundleId);
			if (filterText != null) {
				boolean filtered = false;
				for (String arg : args) {
					if (status.getSymbolicName().indexOf(arg) < 0)
						filtered = true;
				}
				if (filtered)
					continue;
			}

			context.printf("[%3d] %-41s %s\t  %s\n", bundleId, status.getSymbolicName(), status.getVersion(),
					status.getStateName());
		}
	}

	@ScriptUsage(description = "print bundle location", arguments = { @ScriptArgument(name = "bundle id",
			description = "the bundle id") })
	public void location(String[] args) {
		try {
			long bundleId = Long.parseLong(args[0]);
			String location = manager.getBundleLocation(bundleId);
			context.println(location);
		} catch (IllegalStateException e) {
			context.println("bundle not found");
		}
	}

	@ScriptUsage(description = "list bundle resources", arguments = {
			@ScriptArgument(name = "bundle id", description = "the bundle id"),
			@ScriptArgument(name = "directory", description = "directory path", optional = true) })
	public void resources(String[] args) {
		long bundleId = Long.parseLong(args[0]);
		String directory = "/";

		if (args.length == 2) {
			directory = args[1];
		}

		context.println("Bundle Resources");
		context.println("-------------------");
		try {
			List<String> paths = manager.getEntryPaths(bundleId, directory);
			for (String path : paths)
				context.println(path.toString());
		} catch (IllegalStateException e) {
			context.println("bundle not found.");
		}
	}

	@ScriptUsage(description = "view resource's content (only utf-8 encoding supported now)", arguments = {
			@ScriptArgument(name = "bundle id", description = "the bundle id"),
			@ScriptArgument(name = "path", description = "the resource path") })
	public void resource(String[] args) {
		long bundleId = Long.parseLong(args[0]);
		String path = args[1];
		try {
			context.println(manager.getEntry(bundleId, path).replaceAll("%", "%%%%").replaceAll("\n", "\r\n"));
		} catch (IllegalStateException e) {
			context.println("bundle not found");
		} catch (FileNotFoundException e) {
			context.println("file not found");
		} catch (IOException e) {
			context.println("error occurred while file reading.");
		}
	}

	private void drawLine(int length) {
		for (int i = 0; i < length; ++i) {
			context.print("-");
		}
		context.println("");
	}

	public void manifest(String[] args) {
		int bundleId = Integer.parseInt(args[0]);

		BundleContext bundleContext = Kraken.getContext();
		Bundle bundle = bundleContext.getBundle(bundleId);
		Enumeration<?> enumeration = bundle.getHeaders().keys();
		while (enumeration.hasMoreElements()) {
			Object key = enumeration.nextElement();
			Object value = bundle.getHeaders().get(key);
			context.printf("%s: %s\n", key, value);
		}

		Object lastModified = bundle.getHeaders().get("Bnd-LastModified");
		if (lastModified != null) {
			context.printf("%s\n", new Date(Long.parseLong((String) lastModified)));
		}
	}

	public void timestamp(String[] args) {
		int bundleId = -1;
		String filterText = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

		if (args.length != 0) {
			try {
				bundleId = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				filterText = args[0];
			}
		}

		if (bundleId != -1) {
			Bundle bundle = Kraken.getContext().getBundle(bundleId);
			if (bundle == null) {
				context.println("bundle [" + bundleId + "] not found");
				return;
			}

			Object object = bundle.getHeaders().get("Bnd-LastModified");
			if (object == null) {
				context.printf("Bnd-LastModified not found in bundle manifest.\n");
				return;
			}
			Date lastModified = new Date(Long.parseLong((String) object));
			context.println(dateFormat.format(lastModified));
		} else {
			Map<Long, BundleStatus> bundles = manager.getBundles();
			context.println("[ ID] Symbolic Name\t\t\t\tVersion\t  Build Timestamp");
			drawLine(80);

			Set<Long> sortedKeys = new TreeSet<Long>(bundles.keySet());
			for (Long key : sortedKeys) {
				BundleStatus status = bundles.get(key);
				if (filterText != null) {
					boolean filtered = false;
					for (String arg : args) {
						if (status.getSymbolicName().indexOf(arg) < 0)
							filtered = true;
					}
					if (filtered)
						continue;
				}

				Date buildTimestamp = status.getBuildTimestamp();
				context.printf("[%3d] %-41s %s\t  %s\n", key, status.getSymbolicName(), status.getVersion(),
						buildTimestamp == null ? "N/A" : dateFormat.format(buildTimestamp));
			}
		}
	}

	@ScriptUsage(description = "print installable bundle versions", arguments = {
			@ScriptArgument(name = "group id", description = "bundle group id"),
			@ScriptArgument(name = "artifact id", description = "bundle artifact id") })
	public void versions(String[] args) {
		BundleContext bc = Kraken.getContext();
		ServiceReference ref = bc.getServiceReference(KeyStoreManager.class.getName());
		KeyStoreManager keyman = (KeyStoreManager) bc.getService(ref);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");

		for (BundleRepository repo : manager.getRemoteRepositories()) {
			try {
				MavenMetadata mm = new MavenMetadata(repo, keyman, args[0], args[1]);

				if (mm.getRelease() != null || mm.getLatest() != null || mm.getVersions() != null) {
					context.println("Repository [" + repo + "]");
					if (mm.getRelease() != null)
						context.println("\trelease: " + mm.getRelease());
					if (mm.getLatest() != null)
						context.println("\tlatest: " + mm.getLatest());
					if (mm.getVersions() != null)
						context.println("\tversions: " + mm.getVersions());
					if (mm.getLastUpdated() != null)
						context.println("\tlast updated: " + dateFormat.format(mm.getLastUpdated()));
				}
			} catch (Exception e) {
			}
		}
	}
}
