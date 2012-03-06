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
package org.krakenapps.jpa;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.persistence.PersistenceException;

import org.krakenapps.api.BundleManager;
import org.krakenapps.api.MavenResolveException;
import org.krakenapps.api.ProgressMonitor;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.osgi.framework.BundleException;

/**
 * Kraken Script for JPA control
 * 
 * @author xeraph
 * 
 */
public class JpaScript implements Script {
	private JpaService jpa;
	private ScriptContext context;
	private BundleManager bundleManager;

	/**
	 * Created with the specific JPA service
	 * 
	 * @param jpa
	 *            the JPA service
	 */
	public JpaScript(JpaService jpa, BundleManager bundleManager) {
		this.jpa = jpa;
		this.bundleManager = bundleManager;
	}

	/**
	 * Set script context
	 */
	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	/**
	 * List all registered JPA entity manager factories
	 * 
	 * @param args
	 *            empty array
	 */
	@ScriptUsage(description = "List all registered JPA entity manager factories")
	public void list(String[] args) {
		context.println("JPA Profiles");
		context.println("---------------");
		Set<String> factoryNames = jpa.getProfileNames();
		for (String factoryName : factoryNames) {
			context.println(factoryName);
		}
	}

	@ScriptUsage(description = "List all registered JPA entity manager factories", arguments = { @ScriptArgument(name = "factory name", type = "string", description = "Print all properties of the specified entity manager factory") })
	public void props(String[] args) {
		String factoryName = args[0];
		JpaProfile config = jpa.getProfile(factoryName);
		if (config == null) {
			context.println("entity manager factory not found");
			return;
		}

		Properties props = config.getProperties();
		for (Object key : props.keySet()) {
			context.printf("%s = %s\n", key, props.get(key));
		}

		context.println("");
	}

	@ScriptUsage(description = "Create and register new entity manager factory", arguments = {
			@ScriptArgument(name = "Bundle ID", type = "int", description = "Bundle that contains JPA model classes and kraken-jpa configurations"),
			@ScriptArgument(name = "Entity Manager Factory Name", type = "string", description = "Alias for entity manager factory") })
	public void configure(String[] args) {
		int bundleId = Integer.parseInt(args[0]);
		String factoryName = args[1];
		Properties props = new Properties();

		try {
			context.println("Config Templates");
			context.println("------------------");

			int i = 1;
			List<DatabaseConfigTemplate> templates = getConfigTemplates();
			for (DatabaseConfigTemplate t : templates)
				context.println("[" + (i++) + "] " + t.toString());

			context.print("select? ");
			int selected = Integer.valueOf(context.readLine());

			if (selected < 1 && selected > templates.size()) {
				context.println("invalid number");
				return;
			}

			DatabaseConfigTemplate t = templates.get(selected - 1);

			context.print("Host (default: localhost)? ");
			String host = context.readLine();

			context.print("Database? ");
			String db = context.readLine().trim();
			if (db.isEmpty()) {
				context.println("=> database name is empty");
				return;
			}

			context.print("User? ");
			String user = context.readLine().trim();
			if (user.isEmpty())
				context.println("=> user will be JPA config default");

			context.print("Password? ");
			String password = context.readPassword().trim();
			if (password.isEmpty())
				context.println("=> password will be JPA config default");

			t.set(props, host, db, user, password);

			jpa.registerEntityManagerFactory(factoryName, props, bundleId);

			context.println(factoryName + " registered");
		} catch (NumberFormatException e) {
			context.println("invalid number format");
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	private List<DatabaseConfigTemplate> getConfigTemplates() {
		File jpaDir = new File(System.getProperty("kraken.data.dir"), "kraken-jpa");
		jpaDir.mkdirs();
		String jpaDataPath = jpaDir.getAbsolutePath();
		
		List<DatabaseConfigTemplate> configs = new ArrayList<JpaScript.DatabaseConfigTemplate>();
		configs.add(new DatabaseConfigTemplate("MySQL", "com.mysql.jdbc.Driver",
				"org.hibernate.dialect.MySQLInnoDBDialect",
				"jdbc:mysql://$host/$db?useUnicode=true&characterEncoding=utf8"));
		configs.add(new DatabaseConfigTemplate("PostgreSQL", "org.postgresql.Driver",
				"org.hibernate.dialect.PostgreSQLDialect", "jdbc:postgresql://$host/$db"));
		configs.add(new DatabaseConfigTemplate("HSQLDB", "org.hsqldb.jdbcDriver", "org.hibernate.dialect.HSQLDialect",
				"jdbc:hsqldb:" + jpaDataPath + "/$db;shutdown=true"));
		return configs;
	}

	public void installDriver(String[] args) {
		try {
			context.println("Database");
			context.println("-------------");
			List<JDBCDriverBundleInfo> infos = getDriverInfos();
			int i = 1;
			for (JDBCDriverBundleInfo info : infos)
				context.println("[" + (i++) + "] " + info.toString());

			context.print("select? ");
			int selected = Integer.valueOf(context.readLine());

			if (selected < 1 && selected > infos.size()) {
				context.println("invalid number");
				return;
			}

			JDBCDriverBundleInfo info = infos.get(selected - 1);
			long bundleId = bundleManager.installBundle(new ProgressMonitorImpl(context), info.groupId,
					info.artifactId, info.version);
			context.println("bundle [" + bundleId + "] loaded");
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		} catch (MavenResolveException e) {
			context.println("install failed");
		}
	}

	private List<JDBCDriverBundleInfo> getDriverInfos() {
		List<JDBCDriverBundleInfo> infos = new ArrayList<JDBCDriverBundleInfo>();
		infos.add(new JDBCDriverBundleInfo("MySQL", "com.mysql.jdbc", "com.springsource.com.mysql.jdbc", "5.1.6"));
		infos.add(new JDBCDriverBundleInfo("PostgreSQL", "org.postgresql", "com.springsource.org.postgresql.jdbc4",
				"8.3.604"));
		return infos;
	}

	/**
	 * Create and register new entity manager factory with specified JPA model
	 * bundle id and properties for overriding
	 * 
	 * @param args
	 *            model bundle id and alias for entity manager factory
	 */
	@ScriptUsage(description = "Create and register new entity manager factory", arguments = {
			@ScriptArgument(name = "Bundle ID", type = "int", description = "Bundle that contains JPA model classes and kraken-jpa configurations"),
			@ScriptArgument(name = "Entity Manager Factory Name", type = "string", description = "Alias for entity manager factory") })
	public void register(String[] args) {
		int bundleId = Integer.parseInt(args[0]);
		String factoryName = args[1];
		Properties props = new Properties();

		try {
			jpa.registerEntityManagerFactory(factoryName, props, bundleId);
			context.println(factoryName + " registered.");
		} catch (BundleException e) {
			context.println(e.getMessage());
		} catch (PersistenceException e) {
			context.println(e.getMessage());
		} catch (Exception e) {
			context.println(e.toString());
		}
	}

	/**
	 * Unregister and close the entity manager factory in JPA service
	 * 
	 * @param args
	 *            empty array
	 */
	@ScriptUsage(description = "Close and unregister the entity manager", arguments = { @ScriptArgument(name = "Entity Manager Factory Name", type = "string", description = "Alias for entity manager factory") })
	public void unregister(String[] args) {
		String factoryName = args[0];
		if (!jpa.hasEntityManagerFactory(factoryName)) {
			context.println(factoryName + " not found");
			return;
		}

		jpa.unregisterEntityManagerFactory(factoryName);
		context.println(factoryName + " unregistered.");
	}

	private static class DatabaseConfigTemplate {
		private String displayText;
		private String connectionString;
		private String driverClass;
		private String dialectClass;

		public DatabaseConfigTemplate(String displayText, String driverClass, String dialectClass,
				String connectionString) {
			this.displayText = displayText;
			this.driverClass = driverClass;
			this.connectionString = connectionString;
			this.dialectClass = dialectClass;
		}

		public void set(Properties props, String host, String db, String user, String password) {
			String url = connectionString.replace("$host", host).replace("$db", db);
			host = emptyToNull(host);
			db = emptyToNull(db);
			user = emptyToNull(user);
			password = emptyToNull(password);

			props.put("hibernate.dialect", dialectClass);
			props.put("hibernate.connection.driver_class", driverClass);
			props.put("hibernate.connection.url", url);

			if (user != null)
				props.put("hibernate.connection.username", user);
			if (password != null)
				props.put("hibernate.connection.password", password);
		}

		private String emptyToNull(String s) {
			if (s == null || s.isEmpty())
				return null;
			return s;
		}

		@Override
		public String toString() {
			return displayText;
		}
	}

	private static class JDBCDriverBundleInfo {
		private String displayText;
		private String groupId;
		private String artifactId;
		private String version;

		private JDBCDriverBundleInfo(String displayText, String groupId, String artifactId, String version) {
			this.displayText = displayText;
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
		}

		@Override
		public String toString() {
			return displayText;
		}
	}

	private static class ProgressMonitorImpl implements ProgressMonitor {
		private ScriptContext context;

		public ProgressMonitorImpl(ScriptContext context) {
			this.context = context;
		}

		@Override
		public void write(String message) {
			context.print(message);
		}

		@Override
		public void writeln(String message) {
			context.println(message);
		}
	}
}
