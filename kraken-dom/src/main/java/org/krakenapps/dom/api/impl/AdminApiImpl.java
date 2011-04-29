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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.UserExtensionProvider;
import org.krakenapps.dom.api.UserExtensionSchema;
import org.krakenapps.dom.exception.CannotRemoveRequestingAdminException;
import org.krakenapps.dom.exception.InvalidPasswordException;
import org.krakenapps.dom.exception.OrganizationNotFoundException;
import org.krakenapps.dom.exception.AdminNotFoundException;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.ProgramProfile;
import org.krakenapps.dom.model.Role;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "dom-admin-api")
@Provides
@JpaConfig(factory = "dom")
public class AdminApiImpl extends AbstractApi<Admin> implements AdminApi, UserExtensionProvider {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	private static final UserExtensionSchema schema;

	static {
		schema = new UserExtensionSchema();
		schema.register(new UserExtensionSchema.FieldDefinition("user_id", "integer", 0));
		schema.register(new UserExtensionSchema.FieldDefinition("role_id", "integer", 0));
		schema.register(new UserExtensionSchema.FieldDefinition("profile_id", "integer", 0));
		schema.register(new UserExtensionSchema.FieldDefinition("lang", "string", "en"));
		schema.register(new UserExtensionSchema.FieldDefinition("created_at", "date", new Date(0)));
		schema.register(new UserExtensionSchema.FieldDefinition("last_login_at", "date", new Date(0)));
	}

	@Override
	public String getName() {
		return "admin";
	}

	@Override
	public UserExtensionSchema getSchema() {
		return schema;
	}

	@Override
	public Map<String, Object> getExtension(int orgId, int id) {
		Admin admin = getAdmin(orgId, id);
		if (admin == null)
			return null;

		return adminToMap(admin);
	}

	@Override
	public Object get(int orgId, int id, String key) {
		if (!schema.keySet().contains(key))
			return null;

		Admin admin = getAdmin(orgId, id);
		if (admin == null)
			return null;

		return adminToMap(admin).get(key);
	}

	private Map<String, Object> adminToMap(Admin admin) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("user_id", admin.getId());
		m.put("role_id", admin.getRole().getId());
		m.put("profile_id", admin.getProgramProfile().getId());
		m.put("lang", admin.getLang());
		m.put("created_at", admin.getCreateDateTime());
		m.put("last_login_at", admin.getLastLoginDateTime());
		return m;
	}

	@Override
	public void set(int orgId, int adminId, Map<String, Object> values) {
		Admin admin = getAdmin(orgId, adminId);
		if (admin == null)
			return;

		for (String key : values.keySet()) {
			Object value = values.get(key);
			if (key.equals("role")) {
				admin.setRole((Role) value);
			} else if (key.equals("profile")) {
				admin.setProgramProfile((ProgramProfile) value);
			} else if (key.equals("lang")) {
				admin.setLang((String) value);
			}
		}

		updateAdmin(orgId, null, admin);
	}

	@Transactional
	@Override
	public Admin login(String nick, String hash, String nonce) throws AdminNotFoundException, InvalidPasswordException {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			Admin admin = (Admin) em.createQuery("SELECT a FROM Admin a LEFT JOIN a.user u WHERE u.loginName = ?")
					.setParameter(1, nick).getSingleResult();

			if (hash.equals(Sha1.hash(admin.getUser().getPassword() + nonce)) == false)
				throw new InvalidPasswordException();

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

		admin.setCreateDateTime(new Date());

		em.persist(admin);
	}

	@Transactional
	@Override
	public Admin getAdmin(int organizationId, int adminId) {
		EntityManager em = entityManagerService.getEntityManager();

		try {
			return (Admin) em
					.createQuery("SELECT a FROM Admin a LEFT JOIN a.user u WHERE u.organization.id = ? AND u.id = ?")
					.setParameter(1, organizationId).setParameter(2, adminId).getSingleResult();
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

		String hashedPassword = hashPassword(password);
		return admin.getUser().getPassword().equals(hashedPassword);
	}

	@Override
	public String hashPassword(String text) {
		return Sha1.hashPassword(text);
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

		try {
			Admin admin = (Admin) em
					.createQuery("SELECT a FROM Admin a LEFT JOIN a.users WHERE u.organization.id = ? AND a.id = ?")
					.setParameter(1, organizationId).setParameter(2, adminId).getSingleResult();

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
		} catch (NoResultException e) {
			throw new AdminNotFoundException(adminId);
		}
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

			admin.setLastLoginDateTime(targetAdmin.getLastLoginDateTime());
			admin.setProgramProfile(targetAdmin.getProgramProfile());
			em.merge(admin);

			return admin;
		} catch (NoResultException e) {
			throw new AdminNotFoundException(targetAdmin.getId());
		}
	}
}
