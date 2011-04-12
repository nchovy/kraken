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

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.OrganizationParameterApi;
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
	public Collection<OrganizationParameter> getOrganizationParameters(int orgId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM OrganizationParameter o WHERE o.organization.id = ?").setParameter(1, orgId)
				.getResultList();
	}

	@Transactional
	@Override
	public OrganizationParameter getOrganizationParameter(int orgId, String name) {
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

	@Transactional
	@Override
	public OrganizationParameter getOrganizationParameter(int orgId, int orgParameterId) {
		EntityManager em = entityManagerService.getEntityManager();
		OrganizationParameter op = em.find(OrganizationParameter.class, orgParameterId);
		if (op.getOrganization().getId() == orgId)
			return op;
		return null;
	}

	@Override
	public void createOrganizationParameter(int orgId, OrganizationParameter orgParameter) {
		createOrganizationParameterInternal(orgId, orgParameter);
		fireEntityAdded(orgParameter);
	}

	@Transactional
	private void createOrganizationParameterInternal(int orgId, OrganizationParameter orgParameter) {
		EntityManager em = entityManagerService.getEntityManager();
		em.persist(orgParameter);
	}

	@Override
	public void updateOrganizationParameter(int orgId, OrganizationParameter orgParameter) {
		updateOrganizationParameterInternal(orgId, orgParameter);
		fireEntityUpdated(orgParameter);
	}

	@Transactional
	private void updateOrganizationParameterInternal(int orgId, OrganizationParameter orgParameter) {
		EntityManager em = entityManagerService.getEntityManager();
		if (orgParameter.getId() == 0)
			throw new IllegalArgumentException("check organization parameter id");

		OrganizationParameter op = em.find(OrganizationParameter.class, orgParameter.getId());
		if (op.getOrganization().getId() != orgId || orgParameter.getOrganization().getId() != orgId)
			; // TODO
		op.setValue(orgParameter.getValue());
		em.merge(op);
	}

	@Override
	public void removeOrganizationParameter(int orgId, int orgParameterId) {
		OrganizationParameter op = removeOrganizationParameterInternal(orgId, orgParameterId);
		fireEntityRemoved(op);
	}

	@Transactional
	private OrganizationParameter removeOrganizationParameterInternal(int orgId, int orgParameterId) {
		EntityManager em = entityManagerService.getEntityManager();
		OrganizationParameter op = em.find(OrganizationParameter.class, orgParameterId);
		if (op.getOrganization().getId() == orgId) {
			em.remove(op);
			return op;
		}
		return null;
	}
}
