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
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.AdminSettingApi;
import org.krakenapps.dom.exception.AdminNotFoundException;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.AdminSetting;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "dom-admin-setting-api")
@Provides
@JpaConfig(factory = "dom")
public class AdminSettingApiImpl implements AdminSettingApi {
	@Requires
	private AdminApi userApi;

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<AdminSetting> getUserSettings(int organizationId, int userId) {
		Admin user = userApi.getAdminByUser(organizationId, userId);
		if (user == null)
			throw new AdminNotFoundException(userId);

		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM UserSetting s WHERE s.user.id = ?").setParameter(1, user.getId()).getResultList();
	}

	@Transactional
	@Override
	public String getUserSetting(int organizationId, int userId, String name) {
		EntityManager em = entityManagerService.getEntityManager();
		AdminSetting setting = getUserSetting(em, userId, name);
		return setting != null ? setting.getValue() : null;
	}

	@Transactional
	@Override
	public void updateAdminSetting(int organizationId, int userId, String name, String value) {
		EntityManager em = entityManagerService.getEntityManager();
		Admin user = userApi.getAdminByUser(organizationId, userId);
		if (user == null)
			throw new AdminNotFoundException(userId);

		AdminSetting setting = getUserSetting(em, user.getId(), name);
		if (setting != null) {
			setting.setValue(value);
			em.merge(setting);
		} else {
			setting = new AdminSetting();
			setting.setAdmin(user);
			setting.setName(name);
			setting.setValue(value);
			em.persist(setting);
		}
	}

	@Transactional
	@Override
	public void removeUserSetting(int organizationId, int userId, String name) {
		EntityManager em = entityManagerService.getEntityManager();
		Admin user = userApi.getAdminByUser(organizationId, userId);
		if (user == null)
			throw new AdminNotFoundException(userId);

		try {
			AdminSetting setting = getUserSetting(em, user.getId(), name);
			if (setting == null)
				return;

			em.remove(setting);
		} catch (NoResultException e) {
		}
	}

	private AdminSetting getUserSetting(EntityManager em, int userId, String name) {
		try {
			AdminSetting setting = (AdminSetting) em.createQuery("FROM UserSetting s WHERE s.user.id = ? AND s.name = ?")
					.setParameter(1, userId).setParameter(2, name).getSingleResult();
			return setting;
		} catch (NoResultException e) {
			return null;
		}
	}

}
