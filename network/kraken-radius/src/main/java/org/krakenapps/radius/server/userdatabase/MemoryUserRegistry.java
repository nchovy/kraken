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

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;

@Component(name = "radius-mem-user-registry")
@Provides
public class MemoryUserRegistry implements LocalUserRegistry {

	private ConcurrentMap<String, LocalUser> users;

	public MemoryUserRegistry() {
		users = new ConcurrentHashMap<String, LocalUser>();
	}

	@Override
	public Collection<LocalUser> getUsers() {
		return users.values();
	}

	@Override
	public void add(LocalUser user) {
		users.put(user.getLoginName(), user);

	}

	@Override
	public void remove(String loginName) {
		users.remove(loginName);
	}

	@Override
	public boolean verifyPassword(String loginName, String password) {
		if (!users.containsKey(loginName))
			return false;

		LocalUser user = users.get(loginName);
		return user.getPassword().equals(password);
	}
}
