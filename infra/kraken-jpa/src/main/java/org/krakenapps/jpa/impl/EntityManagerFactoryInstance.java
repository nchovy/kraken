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
package org.krakenapps.jpa.impl;

import java.util.Properties;

/**
 * Contain metadata for entity manager factory
 * 
 * @author xeraph
 * 
 */
public class EntityManagerFactoryInstance {
	private long bundleId;
	private String factoryName;
	private Properties properties;

	public EntityManagerFactoryInstance(long bundleId, String factoryName,
			Properties properties) {
		this.bundleId = bundleId;
		this.factoryName = factoryName;
		this.properties = properties;
	}

	public long getBundleId() {
		return bundleId;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public Properties getProperties() {
		return properties;
	}
}
