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
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;

@Component(name = "radius-confdb-user-registry")
@Provides
public class ConfdbUserRegistry implements LocalUserRegistry {

	private ConcurrentMap<String, LocalUser> users;

	@Requires
	private ConfigService conf;

	public ConfdbUserRegistry() {
		users = new ConcurrentHashMap<String, LocalUser>();
	}

	@Validate
	public void start() {
		ConfigDatabase db = conf.ensureDatabase("kraken-radius");
		for (LocalUser user : db.findAll(LocalUser.class).getDocuments(LocalUser.class)) {
			users.put(user.getLoginName(), user);
		}
	}

	@Override
	public Collection<LocalUser> getUsers() {
		return users.values();
	}

	@Override
	public void add(LocalUser user) {

		LocalUser old = users.putIfAbsent(user.getLoginName(), user);
		if (old != null)
			throw new IllegalStateException("duplicated login name: " + user.getLoginName());

		ConfigDatabase db = conf.ensureDatabase("kraken-radius");
		db.add(user);
	}

	@Override
	public void remove(String loginName) {
		users.remove(loginName);

		ConfigDatabase db = conf.ensureDatabase("kraken-radius");
		Config c = db.findOne(LocalUser.class, Predicates.field("login_name", loginName));
		if (c != null)
			db.remove(c);
	}

	@Override
	public boolean verifyPassword(String loginName, String password) {
		if (!users.containsKey(loginName))
			return false;

		LocalUser user = users.get(loginName);
		return user.getPassword().equals(password);
	}
}
