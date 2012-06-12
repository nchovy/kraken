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
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.api.PrimitiveParseCallback;
import org.krakenapps.confdb.ConfigParser;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DOMException;
import org.krakenapps.dom.api.DefaultEntityEventListener;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.EntityEventListener;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.api.Transaction;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.api.UserExtensionProvider;
import org.krakenapps.dom.model.OrganizationUnit;
import org.krakenapps.dom.model.User;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "dom-user-api")
@Provides
public class UserApiImpl extends DefaultEntityEventProvider<User> implements UserApi {
	private final Logger logger = LoggerFactory.getLogger(UserApiImpl.class.getName());
	private static final Class<User> cls = User.class;
	private static final String NOT_FOUND = "user-not-found";
	private static final String ALREADY_EXIST = "user-already-exist";
	private static final int DEFAULT_SALT_LENGTH = 10;

	private EntityEventListener<OrganizationUnit> orgUnitEventListener = new DefaultEntityEventListener<OrganizationUnit>() {
		@Override
		public void entityRemoving(String domain, OrganizationUnit orgUnit, ConfigTransaction xact, Object state) {
			List<User> users = new ArrayList<User>(getUsers(domain, orgUnit.getGuid(), false));
			List<Predicate> preds = new ArrayList<Predicate>();
			for (User user : users) {
				user.setOrgUnit(null);
				preds.add(getPred(user.getLoginName()));
			}

			Transaction x = Transaction.getInstance(xact);
			if ((state != null) && (state instanceof Boolean) && ((Boolean) state))
				cfg.updates(x, cls, preds, users, null);
			else
				cfg.removes(x, domain, cls, preds, null, UserApiImpl.this);
		}
	};

	@Requires
	private ConfigManager cfg;

	@Requires
	private OrganizationApi orgApi;

	@Requires
	private OrganizationUnitApi orgUnitApi;

	private UserCacheSync cacheSync;
	private ConcurrentMap<String, List<User>> domainUserCache;

	private UserExtensionProviderTracker tracker;
	private ConcurrentMap<String, UserExtensionProvider> userExtensionProviders = new ConcurrentHashMap<String, UserExtensionProvider>();

	public UserApiImpl(BundleContext bc) {
		this.cacheSync = new UserCacheSync();
		this.tracker = new UserExtensionProviderTracker(bc);
		domainUserCache = new ConcurrentHashMap<String, List<User>>();
	}

	@Validate
	public void validate() {
		cfg.setParser(User.class, new UserConfigParser());
		orgUnitApi.addEntityEventListener(orgUnitEventListener);
		this.addEntityEventListener(cacheSync);
		tracker.open();

		// load localhost (TODO: load all other domains)
		List<User> cachedUsers = Collections.synchronizedList(new ArrayList<User>());
		cachedUsers.addAll(cfg.all("localhost", cls));
		domainUserCache.put("localhost", cachedUsers);
	}

	@Invalidate
	public void invalidate() {
		removeEntityEventListener(cacheSync);
		if (orgUnitApi != null)
			orgUnitApi.removeEntityEventListener(orgUnitEventListener);
		tracker.close();
	}

	private Predicate getPred(String loginName) {
		return Predicates.field("loginName", loginName);
	}

	private List<Predicate> getPreds(List<User> users) {
		if (users == null)
			return new ArrayList<Predicate>();

		List<Predicate> preds = new ArrayList<Predicate>(users.size());
		for (User user : users)
			preds.add(getPred(user.getLoginName()));
		return preds;
	}

	private List<User> ensureDomainUserCache(String domain) {
		List<User> userCache = domainUserCache.get(domain);
		if (userCache == null) {
			userCache = Collections.synchronizedList(new ArrayList<User>());
			domainUserCache.put(domain, userCache);
		}
		return userCache;
	}

	@Override
	public Collection<User> getUsers(String domain) {
		return new ArrayList<User>(ensureDomainUserCache(domain));
	}

	@Override
	public Collection<User> getUsers(String domain, Collection<String> loginNames) {
		List<User> filteredUsers = new ArrayList<User>();
		for (User user : ensureDomainUserCache(domain)) {
			if (loginNames.contains(user.getLoginName()))
				filteredUsers.add(user);
		}
		return filteredUsers;
	}

	@Override
	public Collection<User> getUsers(String domain, String orgUnitGuid, boolean includeChildren) {
		List<User> cachedUsers = ensureDomainUserCache(domain);
		List<User> filteredUsers = new ArrayList<User>();

		for (User user : cachedUsers) {
			if (orgUnitGuid == null && user.getOrgUnit() == null)
				filteredUsers.add(user);
			else if (orgUnitGuid != null && user.getOrgUnit() != null && orgUnitGuid.equals(user.getOrgUnit().getGuid()))
				filteredUsers.add(user);
		}

		if (includeChildren) {
			if (orgUnitGuid == null)
				return getUsers(domain);

			OrganizationUnit parent = orgUnitApi.getOrganizationUnit(domain, orgUnitGuid);
			for (OrganizationUnit ou : parent.getChildren())
				filteredUsers.addAll(getUsers(domain, ou.getGuid(), includeChildren));
		}
		return filteredUsers;
	}

	// TODO: remove this broken func
	@Override
	public Collection<User> getUsers(String domain, String domainController) {
		return cfg.all(domain, cls, Predicates.field("domainController", domainController));
	}

	@Override
	public User findUser(String domain, String loginName) {
		List<User> cachedUsers = ensureDomainUserCache(domain);
		for (User user : cachedUsers)
			if (user.getLoginName().equals(loginName))
				return user;

		return null;
	}

	@Override
	public User getUser(String domain, String loginName) {
		User user = findUser(domain, loginName);
		if (user == null)
			throw new DOMException(NOT_FOUND);
		return user;
	}

	@Override
	public void createUsers(String domain, Collection<User> users) {
		createUsers(domain, users, false);
	}

	@Override
	public void createUsers(String domain, Collection<User> users, boolean noHash) {
		if (users == null || users.size() == 0)
			return;

		List<User> userList = new ArrayList<User>(users);
		if (!noHash) {
			for (User user : users) {
				user.setSalt(createSalt(domain));
				user.setPassword(hashPassword(user.getSalt(), user.getPassword()));
			}
		}
		cfg.adds(domain, cls, getPreds(userList), userList, ALREADY_EXIST, this);
	}

	@Override
	public void createUser(String domain, User user) {
		createUser(domain, user, false);
	}

	@Override
	public void createUser(String domain, User user, boolean noHash) {
		if (!noHash) {
			user.setSalt(createSalt(domain));
			user.setPassword(hashPassword(user.getSalt(), user.getPassword()));
		}
		cfg.add(domain, cls, getPred(user.getLoginName()), user, ALREADY_EXIST, this);
	}

	@Override
	public void updateUsers(String domain, Collection<User> users, boolean updatePassword) {
		if (users == null || users.size() == 0)
			return;

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
		// for backward compatibility
		if (user.getLastPasswordChange() == null)
			user.setLastPasswordChange(new Date());

		user.setUpdated(new Date());
		if (updatePassword)
			user.setPassword(hashPassword(user.getSalt(), user.getPassword()));
		cfg.update(domain, cls, getPred(user.getLoginName()), user, NOT_FOUND, this);
	}

	@Override
	public void removeUsers(String domain, Collection<String> loginNames) {
		if (loginNames == null || loginNames.size() == 0)
			return;

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

		int saltLength = getSaltLength(domain);
		logger.trace("kraken dom: salt length [{}]", saltLength);

		for (int i = 0; i < saltLength; i++)
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

	private class UserConfigParser extends ConfigParser {
		@SuppressWarnings("unchecked")
		@Override
		public Object parse(Object obj, PrimitiveParseCallback callback) {
			if (!(obj instanceof Map))
				return null;

			User user = new User();
			Map<String, Object> m = (Map<String, Object>) obj;
			user.setLoginName((String) m.get("login_name"));
			if (m.get("org_unit") != null)
				user.setOrgUnit(callback.onParse(OrganizationUnit.class, (Map<String, Object>) m.get("org_unit")));
			user.setName((String) m.get("name"));
			user.setDescription((String) m.get("description"));
			user.setPassword((String) m.get("password"));
			user.setSalt((String) m.get("salt"));
			user.setTitle((String) m.get("title"));
			user.setEmail((String) m.get("email"));
			user.setPhone((String) m.get("phone"));
			user.setExt((Map<String, Object>) m.get("ext"));
			user.setCreated((Date) m.get("created"));
			user.setUpdated((Date) m.get("updated"));
			user.setLastPasswordChange((Date) m.get("last_password_change"));
			return user;
		}
	}

	private class UserCacheSync extends DefaultEntityEventListener<User> {

		@Override
		public void entityAdded(String domain, User obj, Object state) {
			List<User> cachedUsers = ensureDomainUserCache(domain);
			cachedUsers.add(obj);
		}

		@Override
		public void entityUpdated(String domain, User obj, Object state) {
			List<User> cachedUsers = ensureDomainUserCache(domain);
			for (User cachedUser : cachedUsers)
				if (cachedUser.getLoginName().equals(obj.getLoginName()))
					User.shallowCopy(obj, cachedUser);
		}

		@Override
		public void entityRemoved(String domain, User obj, Object state) {
			List<User> cachedUsers = ensureDomainUserCache(domain);
			Iterator<User> it = cachedUsers.iterator();
			while (it.hasNext()) {
				User user = it.next();
				if (obj.getLoginName().equals(user.getLoginName()))
					it.remove();
			}
		}
	}
}
