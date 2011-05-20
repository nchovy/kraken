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

public enum RadiusModuleType {
	Authenticator("auth", RadiusAuthenticatorFactory.class, RadiusAuthenticator.class, "authenticators"), UserDatabase(
			"userdb", RadiusUserDatabaseFactory.class, RadiusUserDatabase.class, "user_databases");

	RadiusModuleType(String alias, Class<?> factoryClass, Class<?> instanceClass, String configNamespace) {
		this.alias = alias;
		this.factoryClass = factoryClass;
		this.instanceClass = instanceClass;
		this.configNamespace = configNamespace;
	}

	public String getAlias() {
		return alias;
	}

	public Class<?> getFactoryClass() {
		return factoryClass;
	}

	public Class<?> getInstanceClass() {
		return instanceClass;
	}

	public String getConfigNamespace() {
		return configNamespace;
	}

	public static RadiusModuleType parse(String s) {
		for (RadiusModuleType t : values())
			if (t.getAlias().equalsIgnoreCase(s))
				return t;
		return null;
	}

	private String alias;
	private Class<?> factoryClass;
	private Class<?> instanceClass;
	private String configNamespace;
}