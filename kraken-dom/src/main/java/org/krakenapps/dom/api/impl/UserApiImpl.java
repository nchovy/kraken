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
package org.krakenapps.dom.api.impl;

import java.util.Collection;
import java.util.Date;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.OrganizationUnit;
import org.krakenapps.dom.model.User;

@Component(name = "dom-user-api")
@Provides
public class UserApiImpl extends DefaultEntityEventProvider<User> implements UserApi {
	private static final Class<User> cls = User.class;
	private static final String NOT_FOUND = "user-not-found";
	private static final String ALREADY_EXIST = "user-already-exist";
	private static final int DEFAULT_SALT_LENGTH = 10;

	@Requires
	private ConfigManager cfg;

	@Requires
	private OrganizationApi orgApi;

	@Requires
	private OrganizationUnitApi orgUnitApi;

	private Predicate getPred(String loginName) {
		return Predicates.field("loginName", loginName);
	}

	@Override
	public Collection<User> getUsers(String domain) {
		return cfg.all(domain, cls);
	}

	@Override
	public Collection<User> getUsers(String domain, Collection<String> loginNames) {
		return cfg.all(domain, cls, Predicates.in("loginName", loginNames));
	}

	@Override
	public Collection<User> getUsers(String domain, String orgUnitGuid, boolean includeChildren) {
		Collection<User> users = cfg.all(domain, cls, Predicates.field("orgUnit/guid", orgUnitGuid));
		if (includeChildren) {
			OrganizationUnit parent = orgUnitApi.getOrganizationUnit(domain, orgUnitGuid);
			for (OrganizationUnit ou : parent.getChildren())
				users.addAll(getUsers(domain, ou.getGuid(), includeChildren));
		}
		return users;
	}

	@Override
	public Collection<User> getUsers(String domain, String domainController) {
		return cfg.all(domain, cls, Predicates.field("domainController", domainController));
	}

	@Override
	public User findUser(String domain, String loginName) {
		return cfg.find(domain, cls, getPred(loginName));
	}

	@Override
	public User getUser(String domain, String loginName) {
		return cfg.get(domain, cls, getPred(loginName), NOT_FOUND);
	}

	@Override
	public void createUser(String domain, User user) {
		cfg.add(domain, cls, getPred(user.getLoginName()), user, ALREADY_EXIST, this);
	}

	@Override
	public void updateUser(String domain, User user) {
		user.setUpdateDateTime(new Date());
		cfg.update(domain, cls, getPred(user.getLoginName()), user, NOT_FOUND, this);
	}

	@Override
	public void removeUser(String domain, String loginName) {
		cfg.remove(domain, cls, getPred(loginName), NOT_FOUND, this);
	}

	@Override
	public void setSaltLength(String domain, int length) {
		if (length < 0 || length > 20)
			throw new IllegalArgumentException("invalid salt length. (valid: 0~20)");
		orgApi.setOrganizationParameter(domain, "salt_length", length);
	}

	@Override
	public int getSaltLength(String domain) {
		Object length = orgApi.getOrganizationParameter(domain, "salt_length");
		if (length == null || !(length instanceof Integer))
			return DEFAULT_SALT_LENGTH;
		return (Integer) length;
	}

	@Override
	public boolean verifyPassword(String domain, String loginName, String password) {
		User user = getUser(domain, loginName);
		String hash = hashPassword(user.getSalt(), password);

		// null check
		if (user.getPassword() == null || hash == null)
			return (password == hash);

		return user.getPassword().equals(hash);
	}

	@Override
	public String hashPassword(String salt, String text) {
		return Sha1.hashPassword(salt, text);
	}
}
