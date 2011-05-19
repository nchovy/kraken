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

import java.util.Set;

public abstract class RadiusInstance {
	private String name;
	private RadiusFactory<?> factory;
	private RadiusConfigurator conf;

	public RadiusInstance(String name, RadiusFactory<?> factory, RadiusConfigurator conf) {
		this.name = name;
		this.factory = factory;
		this.conf = conf;
	}

	public String getName() {
		return name;
	}

	public void start() {
	}

	public void stop() {
	}

	public RadiusFactory<?> getFactory() {
		return factory;
	}

	public Set<String> getConfigNames() {
		return conf.keySet();
	}

	public void setConfig(String name, Object value) {
		conf.put(name, value);
	}

	public Object getConfig(String name) {
		return conf.get(name);
	}
}
