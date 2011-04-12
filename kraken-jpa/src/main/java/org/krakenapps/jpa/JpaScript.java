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

import java.util.Properties;
import java.util.Set;

import javax.persistence.PersistenceException;

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

	/**
	 * Created with the specific JPA service
	 * 
	 * @param jpa
	 *            the JPA service
	 */
	public JpaScript(JpaService jpa) {
		this.jpa = jpa;
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

		jpa.unregisterEntityManagerFactory(factoryName);
		context.println(factoryName + " unregistered.");
	}
}
