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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.krakenapps.api.AlreadyInstalledPackageException;
import org.krakenapps.api.BundleDescriptor;
import org.krakenapps.api.BundleRequirement;
import org.krakenapps.api.MavenResolveException;
import org.krakenapps.api.PackageDescriptor;
import org.krakenapps.api.PackageIndex;
import org.krakenapps.api.PackageManager;
import org.krakenapps.api.PackageMetadata;
import org.krakenapps.api.PackageNotFoundException;
import org.krakenapps.api.PackageRepository;
import org.krakenapps.api.PackageUpdatePlan;
import org.krakenapps.api.PackageVersionHistory;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.api.VersionRange;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(PackageScript.class.getName());
	private ScriptContext context;
	private BundleContext bc;
	private PackageManager packageManager;

	public PackageScript(BundleContext bc, PackageManagerService packageManager) {
		this.bc = bc;
		this.packageManager = packageManager;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void repositories(String[] args) {
		context.println("Package Repository");
		context.println("--------------------");

		for (PackageRepository repo : packageManager.getRepositories()) {
			String alias = repo.getAlias();
			if (repo.isHttps()) {
				context.printf("[%s] %s, trust=%s, key=%s\n", alias, repo.getUrl().toString(), repo.getTrustStoreAlias(),
						repo.getKeyStoreAlias());
			} else {
				if (repo.isAuthRequired())
					alias += " (http-auth)";

				context.printf("[%s] %s\n", alias, repo.getUrl().toString());
			}
		}
	}

	@ScriptUsage(description = "add package repository", arguments = {
			@ScriptArgument(name = "alias", type = "string", description = "the alias of kraken package repository"),
			@ScriptArgument(name = "url", type = "string", description = "the url of kraken package repository") })
	public void addRepository(String[] args) {
		try {
			String alias = args[0];
			URL url = new URL(args[1]);
			packageManager.createRepository(PackageRepository.create(alias, url));
			context.printf("repository [%s] added\n", alias);
		} catch (MalformedURLException e) {
			context.printf("invalid url format\n");
		} catch (RuntimeException e) {
			context.printf("database failure\n");
		}
	}

	@ScriptUsage(description = "add secure package repository", arguments = {
			@ScriptArgument(name = "alias", type = "string", description = "the alias of kraken package repository"),
			@ScriptArgument(name = "url", type = "string", description = "the url of kraken package repository"),
			@ScriptArgument(name = "trust store alias", type = "string", description = "the alias of kraken truststore"),
			@ScriptArgument(name = "key store alias", type = "string", description = "the alias of kraken keystore. if provided, client authentication will be used", optional = true) })
	public void addSecureRepository(String[] args) {
		try {
			String alias = args[0];
			URL url = new URL(args[1]);
			String trustStoreAlias = args[2];
			String keyStoreAlias = args[3];

			packageManager.createRepository(PackageRepository.createHttps(alias, url, trustStoreAlias, keyStoreAlias));
			context.printf("secure repository [%s] added\n", alias);
		} catch (MalformedURLException e) {
			context.printf("invalid url format\n");
		} catch (RuntimeException e) {
			context.printf("database failure\n");
		}
	}

	@ScriptUsage(description = "remove package repository", arguments = { @ScriptArgument(name = "alias", type = "string", description = "the alias of kraken package repository") })
	public void removeRepository(String[] args) {
		String alias = args[0];
		try {
			packageManager.removeRepository(alias);
			context.printf("repository [%s] removed\n", alias);
		} catch (RuntimeException e) {
			context.println("database failure");
		}
	}

	@ScriptUsage(description = "Set credential for repository http authentication", arguments = {
			@ScriptArgument(name = "alias", type = "string", description = "alias of the maven repository"),
			@ScriptArgument(name = "account", type = "string", description = "account for http authentication"),
			@ScriptArgument(name = "password", type = "string", description = "password for http authentication") })
	public void setHttpAuth(String[] args) {
		PackageRepository repo = packageManager.getRepository(args[0]);
		if (repo == null) {
			context.println("package repository [" + args[0] + "] not found");
			return;
		}

		repo.setAccount(args[1]);
		repo.setPassword(args[2]);
		repo.setAuthRequired(true);

		packageManager.updateRepository(repo);
		context.println("ok");
	}

	@ScriptUsage(description = "Reset credential for repository http authentication", arguments = { @ScriptArgument(name = "alias", type = "string", description = "alias of the maven repository") })
	public void resetHttpAuth(String[] args) {
		PackageRepository repo = packageManager.getRepository(args[0]);
		if (repo == null) {
			context.println("package repository [" + args[0] + "] not found");
			return;
		}

		repo.setAccount(null);
		repo.setPassword(null);
		repo.setAuthRequired(false);

		packageManager.updateRepository(repo);
		context.println("ok");
	}

	public void list(String[] args) {
		context.println("Installed Packages");
		context.println("--------------------");

		for (PackageDescriptor desc : packageManager.getInstalledPackages()) {
			context.printf("%s\t\t%s\n", desc.getName(), desc.getVersion());
		}
	}

	public void installables(String[] args) {
		String keyword = (args.length > 0) ? args[0].toLowerCase() : null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		for (PackageIndex list : packageManager.getPackageIndexes()) {
			context.println(String.format("[%s] %s (%s)", list.getRepository().getAlias(), list.getDescription(),
					dateFormat.format(list.getCreated())));
			for (PackageMetadata metadata : list.getPackages()) {
				if (keyword == null || metadata.getName().toLowerCase().contains(keyword)) {
					if (metadata != null) {
						context.println(String.format("\t%s (%s)", metadata.getName(), metadata.getDescription()));
						for (PackageVersionHistory ver : metadata.getVersions())
							context.println(String.format("\t\t%s (%s)", ver.getVersion(),
									dateFormat.format(ver.getLastUpdated())));
					} else {
						context.println("\t" + metadata);
					}
				}
			}
			context.println("");
		}
	}

	public void search(String[] args) {
		// not yet implemented.
	}

	@ScriptUsage(description = "install new package", arguments = {
			@ScriptArgument(name = "package name", type = "string", description = "the name of kraken package"),
			@ScriptArgument(name = "version", type = "string", description = "the version of kraken package", optional = true) })
	public void install(String[] args) {
		String packageName = args[0];
		String version = null;

		if (args.length > 1)
			version = args[1];

		try {
			packageManager.installPackage(packageName, version, new ProgressMonitorImpl(context));
			context.println("");
			context.println("Complete!");
		} catch (AlreadyInstalledPackageException e) {
			context.println("Already installed package.");
			logger.error("kraken core: already installed package [{}]", packageName);
		} catch (PackageNotFoundException e) {
			context.println("Package not found.");
			logger.error("kraken core: package [{}] not found", packageName);
		} catch (MavenResolveException e) {
			context.println("Maven resolver failed: " + e.getMessage());
			logger.error("kraken core: resolver failed", e);
		} catch (UnrecoverableKeyException e) {
			logger.error("kraken core: unrecoverable key", e);
		} catch (KeyManagementException e) {
			logger.error("kraken core: key management error", e);
		} catch (KeyStoreException e) {
			logger.error("kraken core: key store error", e);
		} catch (RuntimeException e) {
			if (e.getCause() instanceof BundleException) {
				context.println("Bundle exception: " + e.getMessage());
				logger.error("kraken core: bundle error", e);
			} else {
				context.println("unknown error: " + e.getMessage());
				logger.error("kraken core: unknown error", e);
			}
		}
	}

	@ScriptUsage(description = "update bundles and install missing bundles", arguments = { @ScriptArgument(name = "package name", type = "string", description = "the name of kraken package") })
	public void repair(String[] args) {
		String packageName = args[0];

		try {
			PackageVersionHistory history = packageManager.getLatestVersion(packageName);
			updatePackageInternal(packageName, history);

		} catch (PackageNotFoundException e) {
			context.println("Package not found");
		} catch (InterruptedException e) {
			context.println("Interrupted");
		} catch (MavenResolveException e) {
			context.println("Maven resolver failed");
			e.printStackTrace();
		} catch (BundleException e) {
			context.println("Bundle exception: " + e.getMessage());
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
	}

	@ScriptUsage(description = "check version and update package", arguments = { @ScriptArgument(name = "package name", type = "string", description = "the name of kraken package") })
	public void update(String[] args) {
		String packageName = args[0];

		try {
			PackageVersionHistory history = packageManager.checkUpdate(packageName);
			if (history == null) {
				PackageDescriptor desc = packageManager.findInstalledPackage(packageName);
				context.printf("%s %s is up to date\n", packageName, desc.getVersion());
				return;
			}

			updatePackageInternal(packageName, history);

		} catch (PackageNotFoundException e) {
			context.println("Package not found");
		} catch (InterruptedException e) {
			context.println("Interrupted");
		} catch (MavenResolveException e) {
			context.println("Maven resolver failed");
			e.printStackTrace();
		} catch (BundleException e) {
			context.println("Bundle exception: " + e.getMessage());
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
	}

	private static class BundleOrder implements Comparator<BundleDescriptor> {
		@Override
		public int compare(BundleDescriptor o1, BundleDescriptor o2) {
			return (int) (o1.getBundleId() - o2.getBundleId());
		}
	}

	private void updatePackageInternal(String packageName, PackageVersionHistory history) throws PackageNotFoundException,
			InterruptedException, MavenResolveException, BundleException, UnrecoverableKeyException, KeyManagementException,
			KeyStoreException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		context.printf("Latest version found -> version: %s, last updated at: %s\n", history.getVersion(),
				dateFormat.format(history.getLastUpdated()));
		context.println("Resolving Dependencies");

		// print summary
		PackageUpdatePlan dep = packageManager.getUpdatePlan(packageName, history.getVersion());

		context.println("[ ID] Symbolic Name\t\t\t\t\tVersion");
		context.println("------------------------------------------------------------------");

		if (dep.getRemainingBundles().size() > 0) {
			context.println("");
			context.println("Remaining Bundles");

			List<BundleDescriptor> bundles = new ArrayList<BundleDescriptor>(dep.getRemainingBundles());
			Collections.sort(bundles, new BundleOrder());
			for (BundleDescriptor bundle : bundles) {
				context.printf("[%3d] %-49s %s\n", bundle.getBundleId(), bundle.getSymbolicName(), bundle.getVersion());
			}
		}

		if (dep.getInstallingBundles().size() > 0) {
			context.println("Installing Bundles");
			for (BundleRequirement req : dep.getInstallingBundles()) {
				VersionRange range = req.getVersionRange();
				if (range.getLow().equals(range.getHigh())) {
					context.printf("      %-49s %s\n", req.getName(), req.getVersionRange().getHigh());
				} else {
					context.printf("      %-49s (%s ~ %s)\n", req.getName(), req.getVersionRange().getLow(), req
							.getVersionRange().getHigh());
				}
			}
		}

		if (dep.getRemovingBundles().size() > 0) {
			context.println("");
			context.println("Removing Bundles");

			List<BundleDescriptor> bundles = new ArrayList<BundleDescriptor>(dep.getRemovingBundles());
			Collections.sort(bundles, new BundleOrder());

			for (BundleDescriptor bundle : bundles) {
				context.printf("[%3d] %-49s %s\n", bundle.getBundleId(), bundle.getSymbolicName(), bundle.getVersion());
			}
		}

		context.println("");
		context.print("Is This OK? [y/N] ");
		String answer = context.readLine();
		if (answer.equalsIgnoreCase("y")) {
			try {
				packageManager.updatePackage(packageName, history.getVersion(), new ProgressMonitorImpl(context));
				context.println("");
				context.println("Complete!");
			} catch (RuntimeException e) {
				context.println("Database error occurred");
			}

		} else {
			context.println("Canceled!");
		}
	}

	@ScriptUsage(description = "uninstall package", arguments = { @ScriptArgument(name = "package name", type = "string", description = "the name of kraken package") })
	public void uninstall(String[] args) {
		String packageName = args[0];
		try {
			PackageDescriptor pkg = packageManager.findInstalledPackage(packageName);
			if (pkg == null) {
				context.println("Package not found");
				return;
			}

			Map<String, List<PackageDescriptor>> dependMap = packageManager.checkUninstallDependency(packageName);

			List<BundleDescriptor> removingBundles = new ArrayList<BundleDescriptor>();
			List<BundleDescriptor> remainingBundles = new ArrayList<BundleDescriptor>();

			// categorize
			List<BundleDescriptor> relatedBundles = new ArrayList<BundleDescriptor>(packageManager.findRelatedBundles(pkg));
			Collections.sort(relatedBundles, new BundleOrder());

			for (BundleDescriptor bundle : relatedBundles) {
				if (dependMap.containsKey(bundle.getSymbolicName()))
					remainingBundles.add(bundle);
				else
					removingBundles.add(bundle);
			}

			// print
			context.println("[ ID] Symbolic Name\t\t\t\t\tVersion");
			context.println("------------------------------------------------------------------");
			context.println("Removing Bundles: ");

			for (BundleDescriptor bundle : removingBundles) {
				context.printf("[%3d] %-49s %s\n", bundle.getBundleId(), bundle.getSymbolicName(), bundle.getVersion());
			}

			if (remainingBundles.size() > 0) {
				context.println("");
				context.println("Remaining Bundles: ");
				for (BundleDescriptor bundle : remainingBundles) {
					List<PackageDescriptor> packages = dependMap.get(bundle.getSymbolicName());

					context.printf("[%3d] %-49s %s\n", bundle.getBundleId(), bundle.getSymbolicName(), bundle.getVersion());
					context.printf("\tused by [%s]\n", toPackageString(packages));
					context.println("");
				}
			}

			context.print("Is This OK? [y/N] ");
			String answer = context.readLine();
			if (answer.equalsIgnoreCase("y")) {
				packageManager.uninstallPackage(packageName, new ProgressMonitorImpl(context));
			}

			context.println("");
			context.println("Complete!");
		} catch (PackageNotFoundException e) {
			context.println("Package not found");
		} catch (InterruptedException e) {
			context.println("Interrupted");
		} catch (RuntimeException e) {
			context.println("Database error occurred");
		}
	}

	public void export(String[] args) {
		context.println(new PackageDescWriter(bc));
	}

	private String toPackageString(List<PackageDescriptor> packages) {
		StringBuilder b = new StringBuilder(1024);

		int i = 0;
		for (PackageDescriptor pkg : packages) {
			if (i++ != 0)
				b.append(", ");

			b.append(pkg.getName());
		}

		return b.toString();
	}
}
