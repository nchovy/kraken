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
import org.krakenapps.radius.client.auth.ChapAuthenticator;
import org.krakenapps.radius.client.auth.PapAuthenticator;
import org.krakenapps.radius.protocol.RadiusPacket;
import org.krakenapps.radius.server.RadiusConfigMetadata;
import org.krakenapps.radius.server.RadiusFactory;
import org.krakenapps.radius.server.RadiusInstance;
import org.krakenapps.radius.server.RadiusModule;
import org.krakenapps.radius.server.RadiusModuleType;
import org.krakenapps.radius.server.RadiusPortType;
import org.krakenapps.radius.server.RadiusProfile;
import org.krakenapps.radius.server.RadiusServer;
import org.krakenapps.radius.server.RadiusVirtualServer;
import org.krakenapps.radius.server.userdatabase.LocalUser;
import org.krakenapps.radius.server.userdatabase.LocalUserRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadiusScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(RadiusScript.class.getName());

	private ScriptContext context;

	private RadiusServer server;
	private LocalUserRegistry userRegistry;

	public RadiusScript(RadiusServer server, LocalUserRegistry userRegistry) {
		this.server = server;
		this.userRegistry = userRegistry;
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
			PapAuthenticator pap = new PapAuthenticator(client, userName, password);
			RadiusPacket response = client.authenticate(pap);
			context.println(response);
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken radius: pap authentication failed", e);
		}
	}

	@ScriptUsage(description = "authenticate using chap method", arguments = {
			@ScriptArgument(name = "server ip", type = "string", description = "radius server ip address"),
			@ScriptArgument(name = "shared secret", type = "string", description = "shared secret"),
			@ScriptArgument(name = "username", type = "string", description = "user name"),
			@ScriptArgument(name = "password", type = "string", description = "password") })
	public void chapauth(String[] args) {
		try {
			InetAddress addr = InetAddress.getByName(args[0]);
			String sharedSecret = args[1];
			String userName = args[2];
			String password = args[3];

			RadiusClient client = new RadiusClient(addr, sharedSecret);
			ChapAuthenticator chap = new ChapAuthenticator(client, userName, password);
			RadiusPacket response = client.authenticate(chap);
			context.println(response);
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken radius: chap authentication failed", e);
		}
	}

	public void profiles(String[] args) {
		context.println("Profiles");
		context.println("-------------");
		for (RadiusProfile profile : server.getProfiles()) {
			context.println(profile);
		}
	}

	public void virtualServers(String[] args) {
		context.println("Virtual Servers");
		context.println("-----------------");
		for (RadiusVirtualServer vs : server.getVirtualServers()) {
			context.println(vs);
		}
	}

	@ScriptUsage(description = "create virtual server", arguments = {
			@ScriptArgument(name = "name", type = "string", description = "name of the virtual server"),
			@ScriptArgument(name = "profile name", type = "string", description = "profile name"),
			@ScriptArgument(name = "port type", type = "string", description = "type 'auth' for authentication, or 'acct' for accounting", optional = true) })
	public void createVirtualServer(String[] args) {
		try {
			String name = args[0];
			String profileName = args[1];
			RadiusPortType portType = RadiusPortType.Authentication;
			if (args.length >= 3)
				portType = RadiusPortType.parse(args[2]);

			RadiusVirtualServer vs = server.createVirtualServer(name, portType, profileName);
			vs.open();

			context.println("opened " + vs.getBindAddress());
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
			context.println("removed");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken radius: cannot remove virtual server", e);
		}
	}

	@ScriptUsage(description = "create profile", arguments = {
			@ScriptArgument(name = "name", type = "string", description = "name of the profile"),
			@ScriptArgument(name = "secret", type = "string", description = "shared secret") })
	public void createProfile(String[] args) {
		try {
			RadiusProfile profile = new RadiusProfile();
			profile.setName(args[0]);
			profile.setSharedSecret(args[1]);

			context.println("---------------------------");
			context.println("Select Authenticators");
			context.println("---------------------------");
			List<String> selectedAuths = select(RadiusModuleType.Authenticator);

			context.println("---------------------------");
			context.println("Select User Databases");
			context.println("---------------------------");
			List<String> selectedUdbs = select(RadiusModuleType.UserDatabase);

			profile.setAuthenticators(selectedAuths);
			profile.setUserDatabases(selectedUdbs);

			server.createProfile(profile);
			context.println("created");
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "remove profile", arguments = { @ScriptArgument(name = "name", type = "string", description = "name of the profile") })
	public void removeProfile(String[] args) {
		server.removeProfile(args[0]);
		context.println("removed");
	}

	public void modules(String[] args) {
		context.println("Module Types");
		context.println("--------------");
		for (RadiusModuleType type : RadiusModuleType.values())
			context.println("[" + type.getAlias() + "] " + type.getInstanceClass().getName());
	}

	@ScriptUsage(description = "list all module factories", arguments = { @ScriptArgument(name = "module type", type = "string", description = "module type ") })
	public void factories(String[] args) {
		context.println("Factories");
		context.println("------------");

		RadiusModuleType type = RadiusModuleType.parse(args[0]);
		if (type == null) {
			context.println("unknown module type");
			return;
		}

		RadiusModule module = server.getModule(type);

		for (RadiusFactory<?> factory : module.getFactories()) {
			context.println("[" + factory.getName() + "] " + factory);
		}
	}

	@ScriptUsage(description = "list all module instances", arguments = { @ScriptArgument(name = "module type", type = "string", description = "module type ") })
	public void instances(String[] args) {
		context.println("Instances");
		context.println("------------");

		RadiusModuleType type = RadiusModuleType.parse(args[0]);
		if (type == null) {
			context.println("unknown module type");
			return;
		}

		RadiusModule module = server.getModule(type);

		for (RadiusInstance instance : module.getInstances()) {
			context.println("[" + instance.getName() + "] " + instance);
		}
	}

	@ScriptUsage(description = "create a module instance", arguments = {
			@ScriptArgument(name = "module type", type = "string", description = "module type "),
			@ScriptArgument(name = "factory name", type = "string", description = "factory name"),
			@ScriptArgument(name = "instance name", type = "string", description = "instance name") })
	public void createInstance(String[] args) {
		RadiusModuleType type = RadiusModuleType.parse(args[0]);
		if (type == null) {
			context.println("unknown module type");
			return;
		}

		String factoryName = args[1];
		String instanceName = args[2];

		RadiusModule module = server.getModule(type);
		RadiusFactory<?> factory = module.getFactory(factoryName);
		if (factory == null) {
			context.println("factory not found");
			return;
		}

		try {
			Map<String, Object> configs = config(factory.getConfigMetadatas());
			RadiusInstance instance = module.createInstance(instanceName, factoryName, configs);
			context.println("created " + instance);
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		} catch (Throwable t) {
			context.println(t.getMessage());
		}
	}

	@ScriptUsage(description = "remove module instance", arguments = {
			@ScriptArgument(name = "module type", type = "string", description = "module type"),
			@ScriptArgument(name = "instance name", type = "string", description = "instance name") })
	public void removeInstance(String[] args) {
		RadiusModuleType type = RadiusModuleType.parse(args[0]);
		if (type == null) {
			context.println("unknown module type");
			return;
		}

		String instanceName = args[1];

		try {
			RadiusModule module = server.getModule(type);
			module.removeInstance(instanceName);
			context.println("removed");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
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

	private List<String> select(RadiusModuleType type) throws InterruptedException {
		// get instance names
		List<String> names = new ArrayList<String>();
		RadiusModule module = server.getModule(type);
		for (RadiusInstance instance : module.getInstances()) {
			names.add(instance.getName());
		}

		// print instance list
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

				// add to select bucket
				String name = names.get(index - 1);
				if (!selected.contains(name)) {
					selected.add(name);
					context.println(name + " added");
				}

			} catch (NumberFormatException e) {
				context.println("");
				throw new RuntimeException("invalid number format");
			}
		}

		return selected;
	}

	public void users(String[] args) {
		context.println("Local Users");
		context.println("-------------");
		for (LocalUser user : userRegistry.getUsers()) {
			context.println(user.toString());
		}
	}

	@ScriptUsage(description = "add user", arguments = {
			@ScriptArgument(name = "login name", type = "string", description = "login name"),
			@ScriptArgument(name = "password", type = "string", description = "password") })
	public void addUser(String[] args) {
		LocalUser user = new LocalUser();
		user.setLoginName(args[0]);
		user.setPassword(args[1]);

		userRegistry.add(user);
		context.println("user added");
	}

	@ScriptUsage(description = "add user", arguments = { @ScriptArgument(name = "login name", type = "string", description = "login name") })
	public void removeUser(String[] args) {
		userRegistry.remove(args[0]);
	}
}
