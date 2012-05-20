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
package org.krakenapps.radius.server.userdatabase;

import org.krakenapps.radius.server.RadiusUserDatabase;
import org.krakenapps.radius.server.RadiusUserDatabaseFactory;

public class LocalUserDatabase extends RadiusUserDatabase {

	private LocalUserRegistry userRegistry;

	public LocalUserDatabase(String name, RadiusUserDatabaseFactory factory, LocalUserRegistry userRegistry) {
		super(name, factory);
		this.userRegistry = userRegistry;
	}

	@Override
	public boolean verifyPassword(String userName, String password) {
		return userRegistry.verifyPassword(userName, password);
	}

	@Override
	public String toString() {
		return "Local User Database";
	}

}
