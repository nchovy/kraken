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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.OrganizationParameterApi;
import org.krakenapps.dom.exception.OrganizationNotFoundException;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.dom.model.OrganizationParameter;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "dom-org-parameter-api")
@Provides
@JpaConfig(factory = "dom")
public class OrganizationParameterApiImpl extends AbstractApi<OrganizationParameter> implements
		OrganizationParameterApi {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public Map<String, String> getOrganizationParameters(int orgId) {
		EntityManager em = entityManagerService.getEntityManager();
		List<OrganizationParameter> orgParameters = em
				.createQuery("FROM OrganizationParameter o WHERE o.organization.id = ?").setParameter(1, orgId)
				.getResultList();

		Map<String, String> m = new HashMap<String, String>();
		for (OrganizationParameter orgParameter : orgParameters)
			m.put(orgParameter.getName(), orgParameter.getValue());
		return m;
	}

	@Transactional
	@Override
	public String getOrganizationParameter(int orgId, String name) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			OrganizationParameter op = (OrganizationParameter) em
					.createQuery("FROM OrganizationParameter o WHERE o.organization.id = ? AND o.name = ?")
					.setParameter(1, orgId).setParameter(2, name).getSingleResult();

			return op.getValue();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public void setOrganizationParameter(int orgId, String name, String value) {
		boolean isUpdate = (getOrganizationParameter(orgId, name) != null);
		OrganizationParameter orgParameter = setOrganizationParameterInternal(orgId, name, value);
		if (isUpdate)
			fireEntityAdded(orgParameter);
		else
			fireEntityAdded(orgParameter);
	}

	@Transactional
	private OrganizationParameter setOrganizationParameterInternal(int orgId, String name, String value) {
		OrganizationParameter orgParameter = getOrgParameter(orgId, name);
		if (orgParameter == null) {
			EntityManager em = entityManagerService.getEntityManager();
			orgParameter = new OrganizationParameter();
			Organization org = em.find(Organization.class, orgId);
			if (org == null)
				throw new OrganizationNotFoundException(orgId);
			orgParameter.setOrganization(org);
			orgParameter.setName(name);
			orgParameter.setValue(value);
			em.persist(orgParameter);
		} else {
			if (orgParameter.getOrganization().getId() != orgId)
				throw new IllegalArgumentException("invalid organization");
			orgParameter.setValue(value);
			EntityManager em = entityManagerService.getEntityManager();
			em.merge(orgParameter);
		}
		return orgParameter;
	}

	@Override
	public void unsetOrganizationParameter(int orgId, String name) {
		OrganizationParameter op = unsetOrganizationParameterInternal(orgId, name);
		if (op != null)
			fireEntityRemoved(op);
	}

	@Transactional
	private OrganizationParameter unsetOrganizationParameterInternal(int orgId, String name) {
		EntityManager em = entityManagerService.getEntityManager();
		OrganizationParameter op = getOrgParameter(orgId, name);
		if (op.getOrganization().getId() == orgId) {
			em.remove(op);
			return op;
		}
		return null;
	}

	@Transactional
	public OrganizationParameter getOrgParameter(int orgId, String name) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			OrganizationParameter op = (OrganizationParameter) em
					.createQuery("FROM OrganizationParameter o WHERE o.organization.id = ? AND o.name = ?")
					.setParameter(1, orgId).setParameter(2, name).getSingleResult();
			return op;
		} catch (NoResultException e) {
			return null;
		}
	}
}
