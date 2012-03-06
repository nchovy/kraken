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

import java.util.Set;

import org.krakenapps.filter.exception.ConfigurationException;

/**
 * Filter is a message processing unit. Filter can receive various message types
 * from binded filters and send messages to binded filters.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public interface Filter {
	/**
	 * Returns the input message specifications that can be bound to this
	 * filter.
	 * 
	 * @return the input message specifications array. null or empty array if no
	 *         types are supported
	 */
	MessageSpec[] getInputMessageSpecs();

	/**
	 * Returns the output message specification that can be bound to this
	 * filter.
	 * 
	 * @return the output message specification. null if no type is supported
	 */
	MessageSpec getOutputMessageSpec();

	/**
	 * Process a message object pushed from other filters. ActiveFilter usually
	 * queues incoming message in this method, rather than processes it
	 * synchronously.
	 */
	void process(Message message);

	/**
	 * Returns a Set of the keys of properties.
	 */
	Set<String> getPropertyKeys();

	/**
	 * Returns the value of specified property.
	 * 
	 * @param key
	 *            the name of the property
	 * @return the value of specified property
	 */
	Object getProperty(String key);

	/**
	 * Sets the property.
	 * 
	 * @param key
	 *            the name of the property
	 * @param value
	 *            the value of the property
	 */
	void setProperty(String key, Object value);

	/**
	 * Removes the specified property.
	 * 
	 * @param key
	 *            the name of the property
	 */
	void unsetProperty(String key);

	/**
	 * Validates current configuration of the filter.
	 * 
	 * @throws ConfigurationException
	 *             if failed to validate
	 */
	void validateConfiguration() throws ConfigurationException;
}
