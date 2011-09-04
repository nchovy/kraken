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
package org.krakenapps.dom.api.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.UserExtensionProvider;
import org.krakenapps.dom.exception.AdminLockedException;
import org.krakenapps.dom.exception.CannotRemoveRequestingAdminException;
import org.krakenapps.dom.exception.InvalidPasswordException;
import org.krakenapps.dom.exception.OrganizationNotFoundException;
import org.krakenapps.dom.exception.AdminNotFoundException;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.Role;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "dom-admin-api")
@Provides
@JpaConfig(factory = "dom")
public class AdminApiImpl extends AbstractApi<Admin> implements AdminApi, UserExtensionProvider {
	private final Logger logger = LoggerFactory.getLogger(AdminApiImpl.class.getName());

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Override
	public String getName() {
		return "admin";
	}

	@Override
	public Admin login(String nick, String hash, String nonce) throws AdminNotFoundException, InvalidPasswordException {
		Admin admin = getAdmin(nick, hash, nonce);

		if (hash.equals(Sha1.hash(admin.getUser().getPassword() + nonce)) == false) {
			updateLoginFailures(admin, false);
			throw new InvalidPasswordException();
		} else
			updateLoginFailures(admin, true);

		return admin;
	}

	@Transactional
	private void updateLoginFailures(Admin admin, boolean success) {
		EntityManager em = entityManagerService.getEntityManager();
		admin = em.find(Admin.class, admin.getId());

		if (success) {
			admin.setLoginFailures(0);
		} else {
			admin.setLoginFailures(admin.getLoginFailures() + 1);
			if (admin.isUseLoginLock() && admin.getLoginFailures() >= admin.getLoginLockCount())
				admin.setEnabled(false);
		}

		em.merge(admin);
	}

	@Transactional
	private Admin getAdmin(String nick, String hash, String nonce) {
		try {
			EntityManager em = entityManagerService.getEntityManager();
			Admin admin = (Admin) em.createQuery("SELECT a FROM Admin a LEFT JOIN a.user u WHERE u.loginName = ?")
					.setParameter(1, nick).getSingleResult();
			if (!admin.isEnabled())
				throw new AdminLockedException();

			admin.setLastLoginDateTime(new Date());
			em.merge(admin);
			return admin;
		} catch (NoResultException e) {
			throw new AdminNotFoundException(nick);
		}
	}

	@Override
	public void createAdmin(int organizationId, Integer requestAdminId, Admin admin) {
		createAdminInternal(organizationId, requestAdminId, admin);
		fireEntityAdded(admin);
	}

	@Transactional
	private void createAdminInternal(int organizationId, Integer requestAdminId, Admin admin) {
		EntityManager em = entityManagerService.getEntityManager();
		Organization organization = em.find(Organization.class, organizationId);
		if (organization == null)
			throw new OrganizationNotFoundException(organizationId);

		if (requestAdminId != null) {
			Admin requestAdmin = em.find(Admin.class, requestAdminId);
			if (requestAdmin.getRole().getLevel() < admin.getRole().getLevel())
				throw new SecurityException("create admin");
		}

		if (admin.getLang() == null)
			admin.setLang("en");
		admin.setCreateDateTime(new Date());

		em.persist(admin);
	}

	@Transactional
	@Override
	public Admin getAdmin(int organizationId, int adminId) {
		EntityManager em = entityManagerService.getEntityManager();
		Admin admin = em.find(Admin.class, adminId);
		if (admin == null || admin.getUser().getOrganization().getId() != organizationId)
			return null;

		return admin;
	}

	@Transactional
	@Override
	public Admin getAdminByUser(int organizationId, int userId) {
		EntityManager em = entityManagerService.getEntityManager();

		try {
			return (Admin) em
					.createQuery("SELECT a FROM Admin a LEFT JOIN a.user u WHERE u.organization.id = ? AND u.id = ?")
					.setParameter(1, organizationId).setParameter(2, userId).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Transactional
	@SuppressWarnings("unchecked")
	@Override
	public List<Admin> getAdmins(int organizationId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("SELECT a FROM Admin a LEFT JOIN a.user u WHERE u.organization.id = ?")
				.setParameter(1, organizationId).getResultList();
	}

	@Override
	public boolean matchPassword(int organizationId, int adminId, String password) {
		Admin admin = getAdmin(organizationId, adminId);
		if (admin == null)
			throw new AdminNotFoundException(adminId);

		String hashedPassword = hashPassword(admin.getUser().getSalt(), password);
		return admin.getUser().getPassword().equals(hashedPassword);
	}

	@Override
	public String hashPassword(String salt, String text) {
		return Sha1.hashPassword(salt, text);
	}

	@Override
	public String hash(String text) {
		return Sha1.hash(text);
	}

	@Override
	public void removeAdmin(int organizationId, Integer requestAdminId, int adminId) {
		Admin admin = removeAdminInternal(organizationId, requestAdminId, adminId);
		fireEntityRemoved(admin);
	}

	@Transactional
	private Admin removeAdminInternal(int organizationId, Integer requestAdminId, int adminId) {
		EntityManager em = entityManagerService.getEntityManager();

		Admin admin = (Admin) em.find(Admin.class, adminId);
		if (admin.getUser().getOrganization().getId() != organizationId)
			throw new AdminNotFoundException(adminId);

		if (requestAdminId != null) {
			Admin requestAdmin = em.find(Admin.class, requestAdminId);
			if (requestAdmin == null)
				throw new AdminNotFoundException(requestAdminId);

			if (requestAdmin.getRole().getLevel() < admin.getRole().getLevel())
				throw new SecurityException("remove admin");

			if (requestAdminId == adminId) {
				throw new CannotRemoveRequestingAdminException(requestAdminId);
			}
		}

		em.remove(admin);

		return admin;
	}

	@Override
	public void updateAdmin(int organizationId, Integer requestAdminId, Admin targetAdmin) {
		Admin admin = updateAdminInternal(organizationId, requestAdminId, targetAdmin);
		fireEntityUpdated(admin);
	}

	@Transactional
	private Admin updateAdminInternal(int organizationId, Integer requestAdminId, Admin targetAdmin) {
		EntityManager em = entityManagerService.getEntityManager();

		try {
			if (requestAdminId != null) {
				Admin requestAdmin = em.find(Admin.class, requestAdminId);
				if (requestAdmin == null)
					throw new AdminNotFoundException(requestAdminId);

				if (requestAdmin.getRole().getLevel() < targetAdmin.getRole().getLevel())
					throw new SecurityException("update admin");
			}

			Admin admin = (Admin) em
					.createQuery("SELECT a FROM Admin a LEFT JOIN a.user u WHERE u.organization.id = ? AND a.id = ?")
					.setParameter(1, organizationId).setParameter(2, targetAdmin.getId()).getSingleResult();

			Role role = em.find(Role.class, targetAdmin.getRole().getId());

			admin.setRole(role);
			admin.setLastLoginDateTime(targetAdmin.getLastLoginDateTime());
			admin.setProgramProfile(targetAdmin.getProgramProfile());
			admin.setUseIdleTimeout(targetAdmin.isUseIdleTimeout());
			admin.setUseLoginLock(targetAdmin.isUseLoginLock());
			admin.setIdleTimeout(targetAdmin.getIdleTimeout());
			admin.setLoginLockCount(targetAdmin.getLoginLockCount());

			if (!admin.isEnabled() && targetAdmin.isEnabled())
				admin.setLoginFailures(0);

			admin.setEnabled(targetAdmin.isEnabled());

			em.merge(admin);

			return admin;
		} catch (NoResultException e) {
			throw new AdminNotFoundException(targetAdmin.getId());
		}
	}
}
