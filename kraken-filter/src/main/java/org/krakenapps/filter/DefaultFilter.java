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
package org.krakenapps.filter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.krakenapps.filter.exception.ConfigurationException;

/**
 * This class provides default implementations for the {@link Filter} interface.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public class DefaultFilter implements Filter {
	private Map<String, Object> properties;

	/**
	 * Creates a new default filter instance.
	 */
	public DefaultFilter() {
		properties = new ConcurrentHashMap<String, Object>();
	}

	/**
	 * No input message specifications are supported by default. Override this.
	 */
	@Override
	public MessageSpec[] getInputMessageSpecs() {
		return null;
	}

	/**
	 * No output message specification is supported by default. Override this.
	 */
	@Override
	public MessageSpec getOutputMessageSpec() {
		return null;
	}

	/**
	 * Empty message processing by default. Override this.
	 */
	@Override
	public void process(Message message) {
	}

	/**
	 * Returns the value of the specified property.
	 */
	@Override
	public Object getProperty(String key) {
		return properties.get(key);
	}

	/**
	 * Returns a key set of properties.
	 */
	@Override
	public Set<String> getPropertyKeys() {
		return new HashSet<String>(properties.keySet());
	}

	/**
	 * Sets a property.
	 */
	@Override
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	/**
	 * Removes the property.
	 */
	@Override
	public void unsetProperty(String key) {
		properties.remove(key);
	}

	/**
	 * Validates current configuration. No check by default. Override this.
	 */
	@Override
	public void validateConfiguration() throws ConfigurationException {
	}

}
