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

import java.util.Map;
import java.util.Set;

/**
 * Represents the communication unit of filter. Composed of header and
 * properties. Message is immutable.
 * 
 * @author xeraph
 * @since 1.0.0
 */
public interface Message {
	/**
	 * Returns the message specification.
	 */
	public MessageSpec getMessageSpec();

	/**
	 * Returns a Set of property keys.
	 */
	public Set<String> keySet();

	/**
	 * Checks if specific key is contained.
	 * 
	 * @param key
	 *            the name of property
	 * @return true if key exists.
	 */
	public boolean containsKey(String key);

	/**
	 * Returns the value of the property
	 * 
	 * @param key
	 *            the name of property
	 * @returns the value of the property. null if not exists.
	 */
	public Object get(String key);

	/**
	 * Returns a Set of header keys.
	 */
	public Set<String> headerKeySet();

	/**
	 * Checks if specific header is contained.
	 * 
	 * @param key
	 *            the name of header
	 * @return true if header key exists
	 */
	public boolean containsHeader(String key);

	/**
	 * Returns the value of the header.
	 * 
	 * @param key
	 *            the name of header
	 * @return the value of the header. null if not exists.
	 */
	public Object getHeader(String key);

	/**
	 * Returns the unmodifiable header map.
	 * 
	 * @return the unmodifiable header map
	 */
	public Map<String, Object> unmodifiableHeaderMap();

	/**
	 * Returns the unmodifiable map.
	 * 
	 * @return the unmodifiable map
	 */
	public Map<String, Object> unmodifiableFieldMap();
}
