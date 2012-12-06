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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
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
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.ObjectBuilder;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.ConfigUpdateRequest;
import org.krakenapps.dom.api.DOMException;
import org.krakenapps.dom.api.DefaultEntityEventListener;
import org.krakenapps.dom.api.DefaultEntityEventProvider;
import org.krakenapps.dom.api.EntityEventListener;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.api.Transaction;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.api.UserConfigParser;
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
	private static final char[] SALT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
	private final Logger logger = LoggerFactory.getLogger(UserApiImpl.class.getName());
	private static final Class<User> cls = User.class;
	private static final String NOT_FOUND = "user-not-found";
	private static final String ALREADY_EXIST = "user-already-exist";
	private static final int DEFAULT_SALT_LENGTH = 10;

	private EntityEventListener<OrganizationUnit> orgUnitEventListener = new DefaultEntityEventListener<OrganizationUnit>() {
		@Override
		public void entityRemoving(String domain, OrganizationUnit orgUnit, ConfigTransaction xact, Object state) {
			List<Config> users = getConfigs(domain, orgUnit.getGuid(), false, null, 0, Integer.MAX_VALUE);

			Transaction x = Transaction.getInstance(xact);
			if ((state != null) && (state instanceof Boolean) && ((Boolean) state)) {
				List<ConfigUpdateRequest<User>> userUpdates = new ArrayList<ConfigUpdateRequest<User>>();

				for (Config c : users) {
					User user = c.getDocument(User.class, cfg.getParseCallback(domain));
					user.setOrgUnit(null);
					userUpdates.add(new ConfigUpdateRequest<User>(c, user));
				}

				updateUsers(domain, userUpdates);
			} else {
				FilterByLoginNames filter = new FilterByLoginNames();
				for (Config user : users) {
					@SuppressWarnings("unchecked")
					Map<String, Object> doc = (Map<String, Object>) user.getDocument();
					filter.addLoginName((String) doc.get("login_name"));
				}

				cfg.removes(x, domain, cls, Arrays.asList((Predicate) filter), null, UserApiImpl.this);
			}
		}
	};

	@Requires
	private ConfigManager cfg;

	@Requires
	private ConfigService conf;

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
		cfg.setParser(User.class, new UserConfigParser());
		orgUnitApi.addEntityEventListener(orgUnitEventListener);
		tracker.open();
	}

	@Invalidate
	public void invalidate() {
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

		FilterByLoginNames filter = new FilterByLoginNames();

		for (User user : users)
			filter.addLoginName(user.getLoginName());

		return Arrays.asList((Predicate) filter);
	}

	@Override
	public int countUsers(String domain, String orgUnitGuid, boolean includeChildren, Predicate pred) {
		if (orgUnitGuid == null && includeChildren)
			return cfg.count(domain, cls, null);

		int total = cfg.count(domain, cls, Predicates.and(Predicates.field("orgUnit/guid", orgUnitGuid), pred));

		if (includeChildren) {
			OrganizationUnit parent = orgUnitApi.getOrganizationUnit(domain, orgUnitGuid);
			for (OrganizationUnit ou : parent.getChildren())
				total += countUsers(domain, ou.getGuid(), includeChildren, pred);
		}

		return total;
	}

	@Override
	public Collection<User> getUsers(String domain) {
		return cfg.all(domain, cls);
	}

	@Override
	public Collection<User> getUsers(String domain, int offset, int limit) {
		return cfg.all(domain, cls, null, offset, limit);
	}

	@Override
	public Collection<User> getUsers(String domain, Collection<String> loginNames) {
		return cfg.all(domain, cls, Predicates.in("loginName", loginNames));
	}

	@Override
	public Collection<User> getUsers(String domain, String orgUnitGuid, boolean includeChildren) {
		return getUsers(domain, orgUnitGuid, includeChildren, null, 0, Integer.MAX_VALUE);
	}

	@Override
	public Collection<User> getUsers(String domain, String orgUnitGuid, boolean includeChildren, Predicate pred, int offset,
			int limit) {
		if (orgUnitGuid == null && includeChildren)
			return cfg.all(domain, cls, pred, offset, limit);

		Collection<User> users = cfg.all(domain, cls, Predicates.and(Predicates.field("orgUnit/guid", orgUnitGuid), pred),
				offset, limit);

		int dec = Math.min(offset, users.size());
		if (offset == dec) // offset <= users
			limit -= users.size() - offset;
		offset -= dec;

		if (includeChildren) {
			OrganizationUnit parent = orgUnitApi.getOrganizationUnit(domain, orgUnitGuid);
			for (OrganizationUnit ou : parent.getChildren()) {
				if (limit <= 0)
					break;

				Collection<User> childUsers = getUsers(domain, ou.getGuid(), includeChildren, pred, offset, limit);
				dec = Math.min(offset, childUsers.size());
				if (offset == dec) // offset <= child users
					limit -= childUsers.size() - offset;
				offset -= dec;

				users.addAll(childUsers);
			}
		}

		return users;
	}

	@Override
	public List<Config> getConfigs(String domain, String orgUnitGuid, boolean includeChildren, Predicate pred, int offset,
			int limit) {
		if (orgUnitGuid == null && includeChildren)
			return cfg.matches(domain, cls, pred, offset, limit);

		List<Config> users = cfg.matches(domain, cls, Predicates.and(Predicates.field("orgUnit/guid", orgUnitGuid), pred),
				offset, limit);

		int dec = Math.min(offset, users.size());
		if (offset == dec) // offset <= users
			limit -= users.size() - offset;
		offset -= dec;

		if (includeChildren) {
			OrganizationUnit parent = orgUnitApi.getOrganizationUnit(domain, orgUnitGuid);
			for (OrganizationUnit ou : parent.getChildren()) {
				if (limit <= 0)
					break;

				List<Config> childUsers = getConfigs(domain, ou.getGuid(), includeChildren, pred, offset, limit);
				dec = Math.min(offset, childUsers.size());
				if (offset == dec) // offset <= child users
					limit -= childUsers.size() - offset;
				offset -= dec;

				users.addAll(childUsers);
			}
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
	public Collection<User> getUsers(String domain, Predicate pred) {
		return cfg.all(domain, cls, pred);
	}

	@Override
	public User getUser(String domain, String loginName) {
		return cfg.get(domain, cls, getPred(loginName), NOT_FOUND);
	}

	@Override
	public Collection<String> getLoginNames(String domain, String orgUnitGuid, boolean includeChildren, Predicate pred,
			int offset, int limit) {
		if (orgUnitGuid == null && includeChildren)
			return cfg.findObjects(domain, cls, new LoginNameFetcher(), pred, offset, limit);

		Collection<String> loginNames = cfg.findObjects(domain, cls, new LoginNameFetcher(),
				Predicates.and(pred, Predicates.field("orgUnit/guid", orgUnitGuid)), offset, limit);

		int dec = Math.min(offset, loginNames.size());
		if (offset == dec) // offset <= users
			limit -= loginNames.size() - offset;
		offset -= dec;

		if (includeChildren) {
			OrganizationUnit parent = orgUnitApi.getOrganizationUnit(domain, orgUnitGuid);
			for (OrganizationUnit ou : parent.getChildren()) {
				if (limit <= 0)
					break;

				Collection<String> childUsers = getLoginNames(domain, ou.getGuid(), includeChildren, pred, offset, limit);
				dec = Math.min(offset, childUsers.size());
				if (offset == dec) // offset <= child users
					limit -= childUsers.size() - offset;
				offset -= dec;

				loginNames.addAll(childUsers);
			}
		}

		return loginNames;
	}

	private class LoginNameFetcher implements ObjectBuilder<String> {
		@Override
		public String build(Config c) {
			@SuppressWarnings("unchecked")
			Map<String, Object> m = (Map<String, Object>) c.getDocument();
			String loginName = (String) m.get("login_name");
			return loginName;
		}
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
			int saltLength = getSaltLength(domain);
			for (User user : users) {
				user.setSalt(createSalt(saltLength));
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
	public void updateUsers(String domain, List<ConfigUpdateRequest<User>> userUpdates) {
		updateUsers(domain, userUpdates, false);
	}

	@Override
	public void updateUsers(String domain, List<ConfigUpdateRequest<User>> userUpdates, boolean updatePassword) {
		if (userUpdates == null || userUpdates.size() == 0)
			return;

		ConfigDatabase db = conf.ensureDatabase("kraken-dom-" + domain);
		Transaction xact = new Transaction(domain, db);
		xact.addEventProvider(cls, this);

		try {
			for (ConfigUpdateRequest<User> update : userUpdates) {
				User user = update.doc;
				user.setUpdated(new Date());
				if (updatePassword)
					user.setPassword(hashPassword(user.getSalt(), user.getPassword()));
				xact.update(update.config, user);
			}
			xact.commit("kraken-dom", "updated " + userUpdates.size() + " users");
		} catch (Throwable e) {
			xact.rollback();
			if (e instanceof DOMException)
				throw (DOMException) e;
			throw new RuntimeException(e);
		}
	}

	@Deprecated
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

		Predicate pred = Predicates.in("login_name", new HashSet<String>(loginNames));
		cfg.removes(domain, cls, Arrays.asList(pred), NOT_FOUND, this);
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
		int saltLength = getSaltLength(domain);
		logger.trace("kraken dom: salt length [{}]", saltLength);

		return createSalt(saltLength);
	}

	private String createSalt(int saltLength) {
		StringBuilder salt = new StringBuilder(saltLength);
		Random rand = new Random();
		for (int i = 0; i < saltLength; i++)
			salt.append(SALT_CHARS[rand.nextInt(SALT_CHARS.length)]);
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

	private static class FilterByLoginNames implements Predicate {

		private HashSet<String> loginNames = new HashSet<String>();

		public void addLoginName(String loginName) {
			loginNames.add(loginName);
		}

		@Override
		public boolean eval(Config c) {
			Object doc = c.getDocument();
			if (doc == null)
				return false;

			@SuppressWarnings("unchecked")
			Map<String, Object> m = (Map<String, Object>) doc;
			return loginNames.contains(m.get("login_name"));
		}
	}
}
