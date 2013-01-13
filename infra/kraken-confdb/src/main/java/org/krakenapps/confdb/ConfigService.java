/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.confdb;

import java.util.List;

public interface ConfigService {
	ConfigDatabase getDatabase(String name);

	ConfigDatabase getDatabase(String name, Integer rev);

	ConfigDatabase ensureDatabase(String name);

	ConfigDatabase createDatabase(String name);

	void dropDatabase(String name);

	List<String> getDatabaseNames();

	void addListener(ConfigServiceListener listener);

	void removeListener(ConfigServiceListener listener);
}
