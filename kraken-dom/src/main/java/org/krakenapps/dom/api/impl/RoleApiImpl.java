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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.RoleApi;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.SecurityManager;
import org.krakenapps.dom.exception.AdminNotFoundException;
import org.krakenapps.dom.model.Role;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "dom-role-api")
@Provides
@JpaConfig(factory = "dom")
public class RoleApiImpl implements RoleApi, SecurityManager {
	@Requires
	private AdminApi userApi;
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Transactional
	@Override
	public Role getRole(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.find(Role.class, id);
	}

	@Transactional
	@Override
	public Role getRole(String name) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			return (Role) em.createQuery("FROM Role r WHERE r.name = ?").setParameter(1, name).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<Role> getGrantableRoles(int organizationId, int userId) {
		EntityManager em = entityManagerService.getEntityManager();
		Admin user = getUser(organizationId, userId);
		int level = user.getRole().getLevel();
		return em.createQuery("FROM Role r WHERE r.level <= ?").setParameter(1, level).getResultList();
	}

	private Admin getUser(int organizationId, int userId) {
		Admin user = userApi.getAdmin(organizationId, userId);
		if (user == null)
			throw new AdminNotFoundException(userId);
		return user;
	}

	@Transactional
	@Override
	public boolean checkPermission(int organizationId, int userId, String permission) {
		EntityManager em = entityManagerService.getEntityManager();
		Admin user = getUser(organizationId, userId);

		try {
			em.createQuery("FROM Permission p LEFT JOIN p.roles r WHERE r.id = ? and p.name = ?")
					.setParameter(1, user.getRole().getId()).setParameter(2, permission).getSingleResult();
			return true;
		} catch (NoResultException e) {
			return false;
		}
	}

}
