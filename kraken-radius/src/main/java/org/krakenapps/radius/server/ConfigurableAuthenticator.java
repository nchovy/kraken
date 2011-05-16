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
package org.krakenapps.radius.server;

import java.util.List;
import java.util.Set;

import org.krakenapps.radius.protocol.AccessRequest;
import org.krakenapps.radius.protocol.RadiusPacket;

public abstract class ConfigurableAuthenticator implements RadiusAuthenticator {

	private String name;
	private RadiusAuthenticatorFactory factory;
	private RadiusConfigurator config;

	public ConfigurableAuthenticator(String name, RadiusAuthenticatorFactory factory, RadiusConfigurator config) {
		this.name = name;
		this.factory = factory;
		this.config = config;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public RadiusPacket authenticate(AccessRequest req, List<RadiusUserDatabase> userDatabases) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RadiusAuthenticatorFactory getFactory() {
		return factory;
	}

	@Override
	public Set<String> getConfigNames() {
		return config.keySet();
	}

	@Override
	public void setConfig(String name, Object value) {
		config.put(name, value);
	}

	@Override
	public Object getConfig(String name) {
		return config.get(name);
	}
}
