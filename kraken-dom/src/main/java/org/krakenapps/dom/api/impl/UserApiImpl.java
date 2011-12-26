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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.EntityEventListener;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.api.UserExtensionProvider;
import org.krakenapps.dom.model.OrganizationUnit;
import org.krakenapps.dom.model.User;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

@Component(name = "dom-user-api")
@Provides
public class UserApiImpl extends DefaultEntityEventProvider<User> implements UserApi, EntityEventListener<OrganizationUnit> {
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

	private UserExtensionProviderTracker tracker;
	private ConcurrentMap<String, UserExtensionProvider> userExtensionProviders = new ConcurrentHashMap<String, UserExtensionProvider>();

	public UserApiImpl(BundleContext bc) {
		this.tracker = new UserExtensionProviderTracker(bc);
	}

	@Validate
	public void validate() {
		orgUnitApi.addEntityEventListener(this);
		tracker.open();
	}

	@Invalidate
	public void invalidate() {
		if (orgUnitApi != null)
			orgUnitApi.removeEntityEventListener(this);
		tracker.close();
	}

	private Predicate getPred(String loginName) {
		return Predicates.field("loginName", loginName);
	}

	private List<Predicate> getPreds(List<User> users) {
		List<Predicate> preds = new ArrayList<Predicate>(users.size());
		for (User user : users)
			preds.add(getPred(user.getLoginName()));
		return preds;
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
			if (orgUnitGuid == null)
				return getUsers(domain);

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
	public void createUsers(String domain, Collection<User> users) {
		List<User> userList = new ArrayList<User>(users);
		for (User user : users) {
			user.setSalt(createSalt(domain));
			user.setPassword(hashPassword(user.getSalt(), user.getPassword()));
		}
		cfg.adds(domain, cls, getPreds(userList), userList, ALREADY_EXIST, this);
	}

	@Override
	public void createUser(String domain, User user) {
		user.setSalt(createSalt(domain));
		user.setPassword(hashPassword(user.getSalt(), user.getPassword()));
		cfg.add(domain, cls, getPred(user.getLoginName()), user, ALREADY_EXIST, this);
	}

	@Override
	public void updateUsers(String domain, Collection<User> users, boolean updatePassword) {
		List<User> userList = new ArrayList<User>(users);
		for (User user : users) {
			user.setUpdated(new Date());
			if (updatePassword)
				user.setPassword(hashPassword(user.getSalt(), user.getPassword()));
		}
		cfg.updates(domain, cls, getPreds(userList), userList, NOT_FOUND, this);
	}

	@Override
	public void updateUser(String domain, User user, boolean updatePassword) {
		user.setUpdated(new Date());
		if (updatePassword)
			user.setPassword(hashPassword(user.getSalt(), user.getPassword()));
		cfg.update(domain, cls, getPred(user.getLoginName()), user, NOT_FOUND, this);
	}

	@Override
	public void removeUsers(String domain, Collection<String> loginNames) {
		List<Predicate> preds = new ArrayList<Predicate>(loginNames.size());
		for (String loginName : loginNames)
			preds.add(getPred(loginName));
		cfg.removes(domain, cls, preds, NOT_FOUND, this);
	}

	@Override
	public void removeUser(String domain, String loginName) {
		cfg.remove(domain, cls, getPred(loginName), NOT_FOUND, this);
	}

	@Override
	public Collection<UserExtensionProvider> getExtensionProviders() {
		return userExtensionProviders.values();
	}

	@Override
	public UserExtensionProvider getExtensionProvider(String name) {
		return userExtensionProviders.get(name);
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
	public String createSalt(String domain) {
		StringBuilder salt = new StringBuilder();
		Random rand = new Random();
		char[] c = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
		for (int i = 0; i < getSaltLength(domain); i++)
			salt.append(c[rand.nextInt(c.length)]);
		return salt.toString();
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

	private class UserExtensionProviderTracker extends ServiceTracker {
		public UserExtensionProviderTracker(BundleContext bc) {
			super(bc, UserExtensionProvider.class.getName(), null);
		}

		@Override
		public Object addingService(ServiceReference reference) {
			UserExtensionProvider p = (UserExtensionProvider) super.addingService(reference);
			userExtensionProviders.put(p.getExtensionName(), p);
			return p;
		}

		@Override
		public void removedService(ServiceReference reference, Object service) {
			UserExtensionProvider p = (UserExtensionProvider) service;
			userExtensionProviders.remove(p.getExtensionName());
			super.removedService(reference, service);
		}
	}

	@Override
	public void entityAdded(String domain, OrganizationUnit orgUnit) {
	}

	@Override
	public void entityUpdated(String domain, OrganizationUnit orgUnit) {
	}

	@Override
	public void entityRemoving(String domain, OrganizationUnit orgUnit) {
	}

	@Override
	public void entityRemoved(String domain, OrganizationUnit orgUnit) {
		Collection<String> loginNames = new ArrayList<String>();
		for (User user : getUsers(domain, orgUnit.getGuid(), false))
			loginNames.add(user.getLoginName());
		removeUsers(domain, loginNames);
	}
}
