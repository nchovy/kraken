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
package org.krakenapps.radius.script;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.radius.client.RadiusClient;
import org.krakenapps.radius.client.auth.PapAuthenticator;
import org.krakenapps.radius.protocol.RadiusPacket;
import org.krakenapps.radius.server.RadiusAuthenticator;
import org.krakenapps.radius.server.RadiusAuthenticatorFactory;
import org.krakenapps.radius.server.RadiusConfigMetadata;
import org.krakenapps.radius.server.RadiusPortType;
import org.krakenapps.radius.server.RadiusProfile;
import org.krakenapps.radius.server.RadiusServer;
import org.krakenapps.radius.server.RadiusUserDatabase;
import org.krakenapps.radius.server.RadiusUserDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadiusScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(RadiusScript.class.getName());

	private RadiusServer server;
	private ScriptContext context;

	public RadiusScript(RadiusServer server) {
		this.server = server;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "authenticate using pap method", arguments = {
			@ScriptArgument(name = "server ip", type = "string", description = "radius server ip address"),
			@ScriptArgument(name = "shared secret", type = "string", description = "shared secret"),
			@ScriptArgument(name = "username", type = "string", description = "user name"),
			@ScriptArgument(name = "password", type = "string", description = "password") })
	public void papauth(String[] args) {
		try {
			InetAddress addr = InetAddress.getByName(args[0]);
			String sharedSecret = args[1];
			String userName = args[2];
			String password = args[3];

			RadiusClient client = new RadiusClient(addr, sharedSecret);
			RadiusPacket response = client.authenticate(new PapAuthenticator(userName, password));
			context.println(response);
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken radius: pap authentication failed", e);
		}
	}

	public void profiles(String[] args) {
		context.println("Profiles");
		context.println("-------------");
		for (RadiusProfile profile : server.getProfiles()) {
			context.println(profile);
		}
	}
	/*
	@ScriptUsage(description = "create virtual server", arguments = {
			@ScriptArgument(name = "name", type = "string", description = "name of the virtual server"),
			@ScriptArgument(name = "port type", type = "string", description = "type 'auth' for authentication, or 'acct' for accounting"),
			@ScriptArgument(name = "profile name", type = "string", description = "profile name") })
	public void createVirtualServer(String[] args) {
		try {
			String name = args[0];
			RadiusPortType portType = RadiusPortType.parse(args[1]);
			String profileName = args[1];

			server.createVirtualServer(name, portType, profileName);
			context.println("created");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken radius: cannot create virtual server", e);
		}
	}

	@ScriptUsage(description = "remove virtual server", arguments = { @ScriptArgument(name = "name", type = "string", description = "name of the virtual server") })
	public void removeVirtualServer(String[] args) {
		try {
			String name = args[0];
			server.removeVirtualServer(name);
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken radius: cannot remove virtual server", e);
		}
	}

	@ScriptUsage(description = "create profile", arguments = {
			@ScriptArgument(name = "name", type = "string", description = "name of the profile"),
			@ScriptArgument(name = "secret", type = "string", description = "shared secret") })
	public void createProfile(String[] args) {
		RadiusProfile profile = new RadiusProfile();
		profile.setName(args[0]);
		profile.setSharedSecret(args[1]);

		List<String> authNames = getAuthenticatorNames();
		List<String> udbNames = getUserDatabaseNames();

		context.println("---------------------------");
		context.println("Select Authenticators");
		context.println("---------------------------");
		List<String> selectedAuths = select(authNames);

		context.println("---------------------------");
		context.println("Select User Databases");
		context.println("---------------------------");
		List<String> selectedUdbs = select(udbNames);

		profile.setAuthenticators(selectedAuths);
		profile.setUserDatabases(selectedUdbs);

		server.createProfile(profile);
		context.println("created");
	}

	@ScriptUsage(description = "remove profile", arguments = { @ScriptArgument(name = "name", type = "string", description = "name of the profile") })
	public void removeProfile(String[] args) {
		server.removeProfile(args[0]);
		context.println("removed");
	}

	public void authenticatorFactories(String[] args) {
		context.println("Authenticator Factories");
		context.println("-------------------------");

		for (RadiusAuthenticatorFactory af : server.getAuthenticatorFactories()) {
			context.println("[" + af.getName() + "] " + af.toString());
		}
	}

	public void authenticators(String[] args) {
		context.println("Authenticators");
		context.println("-------------------");

		for (RadiusAuthenticator auth : server.getAuthenticators()) {
			context.println("[" + auth.getName() + "] " + auth);
		}
	}

	@ScriptUsage(description = "create authenticator", arguments = {
			@ScriptArgument(name = "instance name", type = "string", description = "instance name"),
			@ScriptArgument(name = "factory name", type = "string", description = "factory name") })
	public void createAuthenticator(String[] args) {
		try {
			String instanceName = args[0];
			String factoryName = args[1];
			Map<String, Object> configs = new HashMap<String, Object>();

			// TODO: input required parameters
			config(spec)

			server.createAuthenticator(instanceName, factoryName, configs);
			context.println("created");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken radius: cannot create authenticator", e);
		}
	}

	@ScriptUsage(description = "remove authenticator", arguments = { @ScriptArgument(name = "instance name", type = "string", description = "name of the instance") })
	public void removeAuthenticator(String[] args) {
		String instanceName = args[0];
		server.removeAuthenticator(instanceName);
		context.println("removed");
	}

	public void userDatabaseFactories(String[] args) {
		context.println("User Database Factories");
		context.println("--------------------------");

		for (RadiusUserDatabaseFactory udf : server.getUserDatabaseFactories()) {
			context.println("[" + udf.getName() + "] " + udf.toString());
		}
	}

	public void userDatabases(String[] args) {
		context.println("User Databases");
		context.println("-------------------");

		for (RadiusUserDatabase udb : server.getUserDatabases()) {
			context.println("[" + udb.getName() + "] " + udb);
		}
	}

	@ScriptUsage(description = "create user database", arguments = {
			@ScriptArgument(name = "instance name", type = "string", description = "instance name"),
			@ScriptArgument(name = "factory name", type = "string", description = "factory name") })
	public void createUserDatabase(String[] args) {
		String instanceName = args[0];
		String factoryName = args[1];
		Map<String, Object> configs = new HashMap<String, Object>();

		// TODO: input required parameters

		server.createUserDatabase(instanceName, factoryName, configs);
		context.println("created");
	}

	@ScriptUsage(description = "remove user database", arguments = { @ScriptArgument(name = "instance name", type = "string", description = "name of the instance") })
	public void removeUserDatabase(String[] args) {
		String instanceName = args[0];
		server.removeUserDatabase(instanceName);
		context.println("removed");
	}

	private List<String> getAuthenticatorNames() {
		List<String> names = new ArrayList<String>();
		for (RadiusAuthenticator auth : server.getAuthenticators())
			names.add(auth.getName());
		return names;
	}

	private List<String> getUserDatabaseNames() {
		List<String> names = new ArrayList<String>();
		for (RadiusUserDatabase udb : server.getUserDatabases())
			names.add(udb.getName());
		return names;
	}

	private Map<String, Object> config(List<RadiusConfigMetadata> spec) throws InterruptedException {
		Map<String, Object> configs = new HashMap<String, Object>();
		for (RadiusConfigMetadata metadata : spec) {
			String type = metadata.getType().toString().toLowerCase();
			String def = "";
			if (metadata.getDefaultValue() != null)
				def = "(" + metadata.getDefaultValue() + ") ";

			context.print(metadata.getName() + " in " + type + " type " + def + "? ");
			String line = context.readLine();
			Object value = metadata.getType().parse(line);
			
			configs.put(metadata.getName(), value);
		}
		
		return configs;
	}

	private List<String> select(List<String> names) {
		List<String> selected = new ArrayList<String>();

		int i = 1;
		for (String name : names) {
			context.println("[" + (i++) + "] " + name);
		}
		context.println("---------------------------");

		while (true) {
			try {
				context.print("select index (0 for end): ");
				int index = Integer.valueOf(context.readLine());
				if (index == 0)
					break;

				if (index < 0 || index > names.size()) {
					context.println("no item corresponds to selected index");
					continue;
				}

				String name = names.get(index - 1);
				if (!selected.contains(name)) {
					selected.add(name);
					context.println(name + " added");
				}

			} catch (InterruptedException e) {
				context.println("");
				throw new RuntimeException("interrupted");
			} catch (NumberFormatException e) {
				context.println("");
				throw new RuntimeException("invalid number format");
			}
		}

		return selected;
	}
	*/
}
