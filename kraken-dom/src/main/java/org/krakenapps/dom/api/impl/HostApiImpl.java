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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.AreaApi;
import org.krakenapps.dom.api.HostApi;
import org.krakenapps.dom.exception.AreaNotFoundException;
import org.krakenapps.dom.exception.HostNotFoundException;
import org.krakenapps.dom.exception.HostTypeNotFoundException;
import org.krakenapps.dom.exception.OrganizationNotFoundException;
import org.krakenapps.dom.model.Area;
import org.krakenapps.dom.model.DefaultHostExtension;
import org.krakenapps.dom.model.Host;
import org.krakenapps.dom.model.HostExtension;
import org.krakenapps.dom.model.HostType;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "dom-host-api")
@Provides
@JpaConfig(factory = "dom")
public class HostApiImpl extends AbstractApi<Host> implements HostApi {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;
	@Requires
	private AreaApi areaApi;

	@Transactional
	@Override
	public HostExtension getHostExtension(String className) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			return (HostExtension) em.createQuery("FROM HostExtension e WHERE e.className = ?").setParameter(1,
					className).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Transactional
	@SuppressWarnings("unchecked")
	@Override
	public List<HostType> getHostTypes() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM HostType").getResultList();
	}

	@Transactional
	@SuppressWarnings("unchecked")
	@Override
	public List<HostType> getSentrySupportedHostTypes() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM HostType t WHERE t.isSentrySupported = true").getResultList();
	}

	@Override
	public int createHost(int organizationId, int hostTypeId, int areaId, String name, String description) {
		Host host = createHostInternal(organizationId, hostTypeId, areaId, name, description);
		fireEntityAdded(host);
		return host.getId();
	}

	@Transactional
	private Host createHostInternal(int organizationId, int hostTypeId, int areaId, String name, String description) {
		EntityManager em = entityManagerService.getEntityManager();

		Organization organization = em.find(Organization.class, organizationId);
		if (organization == null)
			throw new OrganizationNotFoundException(organizationId);

		HostType hostType = em.find(HostType.class, hostTypeId);
		if (hostType == null)
			throw new HostTypeNotFoundException();

		Area area = em.find(Area.class, areaId);
		if (area == null)
			throw new AreaNotFoundException(areaId);

		Host host = new Host();
		host.setOrganization(organization);
		host.setArea(area);
		host.setHostType(hostType);
		host.setName(name);
		host.setDescription(description);
		host.setGuid(UUID.randomUUID().toString());

		em.persist(host);

		// add default extensions
		for (DefaultHostExtension def : hostType.getDefaultExtensions()) {
			HostExtension ext = def.getKey().getExtension();
			host.getExtensions().add(ext);
			ext.getHosts().add(host);

			em.merge(ext);
			em.merge(host);
		}

		return host;
	}

	@Override
	public void updateHost(int organizationId, int hostId, String name, String description) {
		Host host = updateHostInternal(organizationId, hostId, name, description);
		fireEntityUpdated(host);
	}

	@Transactional
	private Host updateHostInternal(int organizationId, int hostId, String name, String description) {
		EntityManager em = entityManagerService.getEntityManager();

		try {
			Host host = getHost(organizationId, hostId);
			if (host == null)
				throw new HostNotFoundException();

			host.setName(name);
			host.setDescription(description);

			em.merge(host);

			return host;
		} catch (NoResultException e) {
			throw new HostNotFoundException();
		}
	}

	@Transactional
	@Override
	public void updateHostGuid(int organizationId, int hostId, String guid) {
		EntityManager em = entityManagerService.getEntityManager();

		try {
			Host host = getHost(organizationId, hostId);
			if (host == null)
				throw new HostNotFoundException();

			host.setGuid(guid);
			em.merge(host);

			fireEntityUpdated(host);
		} catch (NoResultException e) {
			throw new HostNotFoundException();
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<Host> getAllHosts() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM Host").getResultList();
	}

	@Transactional
	@SuppressWarnings("unchecked")
	@Override
	public List<Host> getHosts(int organizationId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createNamedQuery("Host.findAll").setParameter(1, organizationId).getResultList();
	}

	@Transactional
	@Override
	public Host getHost(String guid) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			return (Host) em.createQuery("FROM Host h WHERE h.guid = ?").setParameter(1, guid).getSingleResult();
		} catch (NoResultException e) {
		}

		return null;
	}

	@Transactional
	@Override
	public Host getHost(int organizationId, int hostId) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			Host host = (Host) em.createNamedQuery("Host.findById").setParameter(1, organizationId).setParameter(2,
					hostId).getSingleResult();

			host.getExtensions().size(); // force loading
			return host;
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<Host> getHosts(int organizationId, int areaId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createNamedQuery("Host.findByArea").setParameter(1, organizationId).setParameter(2, areaId)
				.getResultList();
	}

	@Transactional
	@Override
	public void moveHost(int organizationId, int hostId, int areaId) {
		EntityManager em = entityManagerService.getEntityManager();

		Host host = getHost(organizationId, hostId);
		if (host == null)
			throw new HostNotFoundException();

		Area area = areaApi.getArea(organizationId, areaId);
		host.setArea(area);

		em.merge(host);
	}

	@Override
	public void removeHost(int organizationId, int hostId) {
		Host host = getHost(organizationId, hostId);
		if (host == null)
			throw new HostNotFoundException();

		fireEntityRemoving(host);
		removeHostInternal(organizationId, hostId);
		fireEntityRemoved(host);
	}

	@Transactional
	private void removeHostInternal(int organizationId, int hostId) {
		EntityManager em = entityManagerService.getEntityManager();
		Host host = getHost(organizationId, hostId);

		host.getExtensions().clear();

		em.remove(host);
	}

	@Transactional
	@Override
	public void mapHostExtensions(int organizationId, int hostId, Set<String> hostExtensionNames) {
		EntityManager em = entityManagerService.getEntityManager();
		Host host = getHost(organizationId, hostId);
		if (host == null)
			throw new HostNotFoundException();

		for (String className : hostExtensionNames) {
			HostExtension ext = findHostExtension(em, className);
			if (ext == null)
				continue;

			host.getExtensions().add(ext);
			ext.getHosts().add(host);

			em.merge(ext);
			em.merge(host);
		}
	}

	private HostExtension findHostExtension(EntityManager em, String className) {
		try {
			HostExtension ext = (HostExtension) em.createQuery("FROM HostExtension e WHERE e.className = ?")
					.setParameter(1, className).getSingleResult();
			return ext;
		} catch (NoResultException e) {
			return null;
		}
	}

	@Transactional
	@Override
	public void unmapHostExtensions(int organizationId, int hostId, Set<String> hostExtensionNames) {
		EntityManager em = entityManagerService.getEntityManager();
		Host host = getHost(organizationId, hostId);
		if (host == null)
			throw new HostNotFoundException();

		for (String className : hostExtensionNames) {
			HostExtension ext = findHostExtension(em, className);
			if (ext == null)
				continue;

			host.getExtensions().remove(ext);
			ext.getHosts().remove(host);

			em.merge(ext);
			em.merge(host);
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<Host> getHostsRecursively(int organizationId, int rootAreaId) {
		Collection<Area> subAreas = areaApi.getSubAreas(organizationId, rootAreaId);
		List<Integer> ids = new ArrayList<Integer>(subAreas.size());
		for (Area area : subAreas)
			ids.add(area.getId());

		if (ids.size() == 0)
			return new ArrayList<Host>();

		EntityManager em = entityManagerService.getEntityManager();
		return em.createNamedQuery("Host.findByAreas").setParameter(1, organizationId).setParameter("ids", ids)
				.getResultList();
	}

}
