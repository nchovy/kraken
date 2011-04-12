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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.PermissionApi;
import org.krakenapps.dom.api.RoleApi;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.Permission;
import org.krakenapps.dom.model.Role;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "dom-permission-api")
@Provides
@JpaConfig(factory = "dom")
public class PermissionApiImpl implements PermissionApi {
	@Requires
	private AdminApi adminApi;

	@Requires
	private RoleApi roleApi;

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Transactional
	@Override
	public boolean isPermitted(int orgId, int adminId, String group, String permission) {
		EntityManager em = entityManagerService.getEntityManager();
		Admin admin = adminApi.getAdmin(orgId, adminId);
		if (admin == null)
			throw new SecurityException("admin not found: " + orgId + ", " + adminId);

		int roleId = admin.getRole().getId();
		Long count = (Long) em.createQuery("SELECT COUNT(*) FROM Permission p JOIN p.roles r WHERE r.id = ?")
				.setParameter(1, roleId).getSingleResult();

		return count > 0;
	}

	@Override
	public Set<Permission> getPermissions(String roleName) {
		Role role = roleApi.getRole(roleName);
		if (role == null)
			throw new IllegalStateException("role not found: " + roleName);

		Set<Permission> p = role.getPermissions();
		p.size(); // enforce fetch
		return p;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Set<Permission> getPermissions(int orgId, int adminId, String group) {
		EntityManager em = entityManagerService.getEntityManager();
		Admin admin = adminApi.getAdmin(orgId, adminId);
		if (admin == null)
			throw new SecurityException("admin not found: " + orgId + ", " + adminId);

		int roleId = admin.getRole().getId();
		List<Permission> list = em.createQuery("SELECT p FROM Permission p JOIN p.roles r WHERE p.group = ? AND r.id = ?")
				.setParameter(1, group).setParameter(2, roleId).getResultList();

		return new HashSet<Permission>(list);
	}

}
