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
package org.krakenapps.datasource;

import java.util.Properties;

public interface DataSource {
	/**
	 * path includes instance name.
	 */
	String getPath();

	/**
	 * Time series, Object, and so on.
	 */
	String getType();

	/**
	 * Instance configurations. for example, host, nic name, etc.
	 */
	Properties getProperties();

	/**
	 * Data composed by common data types. Caller can find parser using type
	 * information, and use data.
	 */
	Object getData();

	/**
	 * Update data and invoke callbacks
	 */
	void update(Object data);

	/**
	 * Add event listener. UI element can receive updated data.
	 */
	void addListener(DataSourceEventListener listener);

	/**
	 * Remove event listener.
	 */
	void removeListener(DataSourceEventListener listener);
}
