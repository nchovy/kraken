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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.api.UserExtensionProvider;
import org.krakenapps.dom.model.OrganizationUnit;
import org.krakenapps.dom.model.User;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

@Component(name = "dom-user-api")
@Provides
@JpaConfig(factory = "dom")
public class UserApiImpl extends AbstractApi<User> implements UserApi {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	private UserExtensionProviderTracker tracker;

	private Map<String, UserExtensionProvider> userExtensionProviders;

	public UserApiImpl(BundleContext bc) {
		tracker = new UserExtensionProviderTracker(bc);
	}

	@Validate
	public void start() {
		userExtensionProviders = new ConcurrentHashMap<String, UserExtensionProvider>();
		tracker.open();
	}

	@Invalidate
	public void stop() {
		tracker.close();
	}

	@Override
	public Collection<UserExtensionProvider> getExtensionProviders() {
		return new ArrayList<UserExtensionProvider>(userExtensionProviders.values());
	}

	@Override
	public UserExtensionProvider getExtensionProvider(String name) {
		return userExtensionProviders.get(name);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<User> getUsers() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM User u").getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<User> getUsers(int orgId, Collection<Integer> idList) {
		if (idList == null || idList.size() == 0)
			return new ArrayList<User>();

		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM User u WHERE u.organization.id = :org AND u.id IN (:users)")
				.setParameter("org", orgId).setParameter("users", idList).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<User> getUsers(int orgId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM User u WHERE u.organization.id = ?").setParameter(1, orgId).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<User> getUsers(int orgId, Integer orgUnitId, boolean includeChildren) {
		EntityManager em = entityManagerService.getEntityManager();
		if (!includeChildren)
			return em.createQuery("FROM User u WHERE u.organizationUnit.id = ?").setParameter(1, orgUnitId)
					.getResultList();
		else
			return getChildren(em, orgId, orgUnitId);
	}

	@SuppressWarnings("unchecked")
	private Collection<User> getChildren(EntityManager em, int orgId, Integer orgUnitId) {
		Collection<User> users = new ArrayList<User>();
		Collection<OrganizationUnit> children = null;
		if (orgUnitId != null) {
			children = em.createQuery("FROM OrganizationUnit o WHERE o.parent.id = ?").setParameter(1, orgUnitId)
					.getResultList();
			users.addAll(em.createQuery("FROM User u WHERE u.organizationUnit.id = ?").setParameter(1, orgUnitId)
					.getResultList());
		} else {
			children = em.createQuery("FROM OrganizationUnit o WHERE o.parent IS NULL").getResultList();
			users.addAll(em.createQuery("FROM User u WHERE u.organizationUnit.id IS NULL").getResultList());
		}

		for (OrganizationUnit child : children)
			users.addAll(getChildren(em, child.getOrganization().getId(), child.getId()));

		return users;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<User> getUsers(String domainController) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM User u WHERE u.domainController = ?").setParameter(1, domainController)
				.getResultList();
	}

	@Transactional
	@Override
	public User getUser(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.find(User.class, id);
	}

	@Transactional
	@Override
	public User getUserByLoginName(String loginName) {
		try {
			EntityManager em = entityManagerService.getEntityManager();
			return (User) em.createQuery("FROM User u WHERE u.loginName = ?").setParameter(1, loginName)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public void createUser(User user) {
		createUserInternal(user);
		fireEntityAdded(user);
	}

	@Transactional
	private void createUserInternal(User user) {
		EntityManager em = entityManagerService.getEntityManager();
		user.setCreateDateTime(new Date());
		user.setUpdateDateTime(new Date());
		em.persist(user);
	}

	@Override
	public void updateUser(User user) {
		updateUserInternal(user);
		fireEntityUpdated(user);
	}

	@Transactional
	private void updateUserInternal(User user) {
		EntityManager em = entityManagerService.getEntityManager();
		if (user.getId() == 0)
			throw new IllegalArgumentException("check user id");

		User u = em.find(User.class, user.getId());
		u.setOrganizationUnit(user.getOrganizationUnit());
		u.setLoginName(user.getLoginName());
		u.setName(user.getName());
		u.setDescription(user.getDescription());
		if (user.getPassword() != null)
			u.setPassword(hashPassword(user.getPassword()));
		u.setTitle(user.getTitle());
		u.setEmail(user.getEmail());
		u.setPhone(user.getPhone());
		u.setDomainController(user.getDomainController());
		u.setUpdateDateTime(new Date());
		u.setAdmin(user.getAdmin());
		em.merge(u);
	}

	@Override
	public void removeUser(int id) {
		User user = removeUserInternal(id);
		fireEntityRemoved(user);
	}

	@Transactional
	private User removeUserInternal(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		User user = em.find(User.class, id);
		em.remove(user);
		return user;
	}

	@Override
	public boolean verifyPassword(String id, String password) {
		User user = getUserByLoginName(id);
		if (user == null)
			return false;

		String hash = hashPassword(password);
		return user.getPassword().equals(hash);
	}

	@Override
	public String hashPassword(String text) {
		return Sha1.hashPassword(text);
	}

	private class UserExtensionProviderTracker extends ServiceTracker {
		public UserExtensionProviderTracker(BundleContext bc) {
			super(bc, UserExtensionProvider.class.getName(), null);
		}

		@Override
		public Object addingService(ServiceReference reference) {
			UserExtensionProvider p = (UserExtensionProvider) super.addingService(reference);
			userExtensionProviders.put(p.getName(), p);
			return p;
		}

		@Override
		public void removedService(ServiceReference reference, Object service) {
			UserExtensionProvider p = (UserExtensionProvider) service;
			userExtensionProviders.remove(p.getName());
			super.removedService(reference, service);
		}
	}
}
