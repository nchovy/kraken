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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.dom.model.OrganizationUnit;
import org.krakenapps.dom.model.User;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "dom-org-unit-api")
@Provides
@JpaConfig(factory = "dom")
public class OrganizationUnitApiImpl extends AbstractApi<OrganizationUnit> implements OrganizationUnitApi {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Requires
	private UserApi userApi;

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<OrganizationUnit> getOrganizationUnits() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM OrganizationUnit o").getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<OrganizationUnit> getOrganizationUnits(Organization org) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM OrganizationUnit o WHERE o.organization = ?").setParameter(1, org).getResultList();
	}

	@Transactional
	@Override
	public OrganizationUnit getOrganizationUnit(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.find(OrganizationUnit.class, id);
	}

	@Transactional
	@Override
	public Collection<OrganizationUnit> getParents(int orgId, int id) {
		OrganizationUnit unit = getOrganizationUnit(id);
		List<OrganizationUnit> parents = new ArrayList<OrganizationUnit>();
		while (unit.getParent() != null) {
			parents.add(unit.getParent());
			unit = unit.getParent();
		}

		return parents;
	}

	@Override
	public OrganizationUnit createOrganizationUnit(OrganizationUnit orgUnit) {
		OrganizationUnit ou = createOrganizationUnitInternal(orgUnit);
		fireEntityAdded(ou);
		return ou;
	}

	@Transactional
	private OrganizationUnit createOrganizationUnitInternal(OrganizationUnit orgUnit) {
		EntityManager em = entityManagerService.getEntityManager();
		orgUnit.setCreateDateTime(new Date());
		em.persist(orgUnit);
		return orgUnit;
	}

	@Override
	public OrganizationUnit updateOrganizationUnit(OrganizationUnit orgUnit) {
		OrganizationUnit ou = updateOrganizationUnitInternal(orgUnit);
		fireEntityUpdated(ou);
		return ou;
	}

	@Transactional
	private OrganizationUnit updateOrganizationUnitInternal(OrganizationUnit orgUnit) {
		EntityManager em = entityManagerService.getEntityManager();
		if (orgUnit.getId() == 0)
			throw new IllegalArgumentException("check organization unit id");

		OrganizationUnit ou = em.find(OrganizationUnit.class, orgUnit.getId());
		ou.setName(orgUnit.getName());
		ou.setParent(orgUnit.getParent());
		ou.setDomainController(orgUnit.getDomainController());
		em.merge(ou);
		return ou;
	}

	@Override
	public OrganizationUnit removeOrganizationUnit(int id) {
		// remove all related users
		OrganizationUnit ou = getOrganizationUnit(id);
		if (ou == null)
			throw new IllegalStateException("organization unit not found: " + id);

		Collection<User> users = userApi.getUsers(ou.getOrganization().getId(), ou.getId(), true);
		for (User user : users)
			userApi.removeUser(user.getId());

		// remove org unit
		OrganizationUnit orgUnit = removeOrganizationUnitInternal(id);
		fireEntityRemoved(orgUnit);
		return orgUnit;
	}

	@Transactional
	private OrganizationUnit removeOrganizationUnitInternal(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		OrganizationUnit orgUnit = em.find(OrganizationUnit.class, id);
		em.remove(orgUnit);
		return orgUnit;
	}

}
