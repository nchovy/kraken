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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.LdapOrganizationalUnitApi;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.exception.OrganizationNotFoundException;
import org.krakenapps.dom.model.LdapOrganizationalUnit;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.dom.model.OrganizationUnit;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "dom-ldap-org-unit-api")
@Provides
@JpaConfig(factory = "dom")
public class LdapOrganizationalUnitApiImpl extends AbstractApi<LdapOrganizationalUnit> implements
		LdapOrganizationalUnitApi {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Requires
	private OrganizationApi oApi;

	@Requires
	private OrganizationUnitApi ouApi;

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<LdapOrganizationalUnit> getLdapOrganizationalUnits() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM LdapOrganizationalUnit l").getResultList();
	}

	@Transactional
	@Override
	public LdapOrganizationalUnit getLdapOrganizationalUnit(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.find(LdapOrganizationalUnit.class, id);
	}

	@Override
	public void createLdapOrganizationalUnit(LdapOrganizationalUnit unit) {
		createLdapOrganizationalUnitInternal(unit);
		fireEntityAdded(unit);
	}

	@Transactional
	private void createLdapOrganizationalUnitInternal(LdapOrganizationalUnit unit) {
		EntityManager em = entityManagerService.getEntityManager();
		unit.setCreateDateTime(new Date());
		em.persist(unit);
	}

	@Override
	public void updateLdapOrganizationalUnit(LdapOrganizationalUnit unit) {
		updateLdapOrganizationalUnitInternal(unit);
		fireEntityUpdated(unit);
	}

	@Transactional
	private void updateLdapOrganizationalUnitInternal(LdapOrganizationalUnit unit) {
		EntityManager em = entityManagerService.getEntityManager();
		if (unit.getId() == 0)
			throw new IllegalArgumentException("check ldap organizational unit id");

		LdapOrganizationalUnit lUnit = em.find(LdapOrganizationalUnit.class, unit.getId());
		lUnit.setProfile(unit.getProfile());
		lUnit.setDistinguishedName(unit.getDistinguishedName());
		em.merge(lUnit);
	}

	@Override
	public void removeLdapOrganizationalUnit(int id) {
		LdapOrganizationalUnit unit = removeLdapOrganizationalUnitInternal(id);
		fireEntityRemoved(unit);
	}

	@Transactional
	private LdapOrganizationalUnit removeLdapOrganizationalUnitInternal(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		LdapOrganizationalUnit unit = em.find(LdapOrganizationalUnit.class, id);
		em.remove(unit);
		return unit;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void sync() {
		EntityManager em = entityManagerService.getEntityManager();
		Collection<LdapOrganizationalUnit> units = getLdapOrganizationalUnits();
		Map<String, Map<String, OrganizationUnit>> before = new HashMap<String, Map<String, OrganizationUnit>>();
		Map<OrganizationUnit, String> parentMap = new HashMap<OrganizationUnit, String>();

		List<OrganizationUnit> ous = em.createQuery("FROM OrganizationUnit o WHERE o.fromLdap = 1").getResultList();
		for (OrganizationUnit ou : ous) {
			String dc = ou.getDomainController();
			String name = ou.getName();

			if (!before.containsKey(dc))
				before.put(dc, new HashMap<String, OrganizationUnit>());
			before.get(dc).put(name, ou);
		}

		for (LdapOrganizationalUnit unit : units) {
			String parentName = null;
			String name = null;
			String dc = null;

			String dn = unit.getDistinguishedName();
			for (String token : dn.split("(?<!\\\\),")) {
				String attr = token.split("=")[0];
				String value = token.split("=")[1];

				if (attr.equals("OU")) {
					if (name == null)
						name = value;
					else if (parentName == null)
						parentName = value;
				} else if (attr.equals("DC")) {
					if (dc == null)
						dc = value;
					else
						dc += "." + value;
				}
			}
			Organization org = findOrg(em, dc);

			OrganizationUnit ou = null;
			boolean isUpdate = false;
			try {
				String q = "FROM OrganizationUnit o WHERE o.fromLdap = 1 AND o.organization = ? AND o.name = ?";
				ou = (OrganizationUnit) em.createQuery(q).setParameter(1, org).setParameter(2, name).getSingleResult();
				if (before.containsKey(ou.getDomainController()))
					before.get(ou.getDomainController()).remove(ou.getName());
				isUpdate = true;
			} catch (NoResultException e) {
				ou = new OrganizationUnit();
				ou.setName(name);
				ou.setDomainController(dc);
				ou.setOrganization(org);
				ou.setFromLdap(true);
			}

			if (parentName != null) {
				try {
					String q = "FROM OrganizationUnit o WHERE o.name = ? AND o.organization = ?";
					OrganizationUnit parent = (OrganizationUnit) em.createQuery(q).setParameter(1, parentName)
							.setParameter(2, ou.getOrganization()).getSingleResult();
					ou.setParent(parent);
				} catch (NoResultException e) {
					parentMap.put(ou, parentName);
				}
			}

			if (isUpdate)
				ouApi.updateOrganizationUnit(ou);
			else
				ouApi.createOrganizationUnit(ou);

			unit.setOrganizationUnit(ou);
			updateLdapOrganizationalUnit(unit);
		}

		for (OrganizationUnit ou : parentMap.keySet()) {
			String parentName = parentMap.get(ou);
			try {
				OrganizationUnit parent = (OrganizationUnit) em
						.createQuery("FROM OrganizationUnit o WHERE o.name = ? AND o.organization = ?")
						.setParameter(1, parentName).setParameter(2, ou.getOrganization()).getSingleResult();
				ou.setParent(parent);
				ouApi.updateOrganizationUnit(ou);
			} catch (NoResultException e) {
			}
		}

		for (String dc : before.keySet()) {
			for (OrganizationUnit ou : before.get(dc).values())
				ouApi.removeOrganizationUnit(ou.getId());
		}
	}

	private Organization findOrg(EntityManager em, String dc) {
		Organization o = null;
		try {
			o = (Organization) em.createQuery("FROM Organization o").getSingleResult();
		} catch (NonUniqueResultException e) {
			if (dc != null) {
				try {
					o = (Organization) em.createQuery("FROM Organization o WHERE o.domainController = ?")
							.setParameter(1, dc).getSingleResult();
				} catch (NoResultException ex) {
					try {
						o = (Organization) em.createQuery("FROM Organization o WHERE o.backupDomainController = ?")
								.setParameter(1, dc).getSingleResult();
					} catch (NoResultException exc) {
						throw new OrganizationNotFoundException(0); // TODO
					}
				}
			}
		} catch (NoResultException e) {
			o = new Organization();
			o.setName("krakenapps");
			oApi.createOrganization(o);
		}

		return o;
	}
}
