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
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.AreaApi;
import org.krakenapps.dom.api.AreaEventCallback;
import org.krakenapps.dom.exception.AreaNotFoundException;
import org.krakenapps.dom.exception.OrganizationNotFoundException;
import org.krakenapps.dom.exception.UndeletableAreaException;
import org.krakenapps.dom.model.Area;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "dom-area-api")
@Provides
@JpaConfig(factory = "dom")
public class AreaApiImpl extends AbstractApi<Area> implements AreaApi {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;
	
	private WeakHashMap<AreaEventCallback, Integer> callbacks;

	public AreaApiImpl() {
		callbacks = new WeakHashMap<AreaEventCallback, Integer>();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<Area> getAllAreas() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM Area").getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<Area> getAllRootAreas() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM Area a WHERE a.parent IS NULL").getResultList();
	}

	@Transactional
	@Override
	public Area getArea(int organizationId, int areaId) {
		EntityManager em = entityManagerService.getEntityManager();
		Area area = em.find(Area.class, areaId);
		if (area == null)
			throw new AreaNotFoundException(areaId);

		if (area.getOrganization().getId() != organizationId)
			throw new AreaNotFoundException(areaId);

		return area;
	}

	@Transactional
	@Override
	public Collection<Area> getSubAreas(int organizationId, int areaId) {
		EntityManager em = entityManagerService.getEntityManager();
		Set<Area> areaSet = new HashSet<Area>();

		Area area = em.find(Area.class, areaId);
		if (area == null || area.getOrganization().getId() != organizationId)
			return areaSet;

		grabAreaRecursively(area, areaSet);

		return areaSet;
	}

	private void grabAreaRecursively(Area area, Set<Area> areaSet) {
		areaSet.add(area);
		if (area.getAreas().size() == 0)
			return;

		for (Area subArea : area.getAreas()) {
			grabAreaRecursively(subArea, areaSet);
		}
	}

	@Transactional
	@Override
	public void moveArea(int organizationId, int newParentId, int id) throws AreaNotFoundException {
		EntityManager em = entityManagerService.getEntityManager();
		Area area = em.find(Area.class, id);
		if (area == null || area.getOrganization().getId() != organizationId)
			throw new AreaNotFoundException(id);

		Area newParent = em.find(Area.class, newParentId);
		if (newParent == null || newParent.getOrganization().getId() != organizationId)
			throw new AreaNotFoundException(newParentId);

		area.getParent().getAreas().remove(area);
		area.setParent(newParent);
		newParent.getAreas().add(area);

		em.merge(area);
	}

	@Transactional
	@Override
	public Area createArea(int organizationId, int parentId, String name, String description)
			throws AreaNotFoundException {
		EntityManager em = entityManagerService.getEntityManager();

		Organization organization = em.find(Organization.class, organizationId);
		if (organization == null)
			throw new OrganizationNotFoundException(organizationId);

		Area parent = getArea(organizationId, parentId, em);
		Area newArea = new Area();
		newArea.setOrganization(organization);
		newArea.setName(name);
		newArea.setDescription(description);
		newArea.setParent(parent);
		newArea.setCreateDateTime(new Date());

		em.persist(newArea);

		for (AreaEventCallback callback : callbacks.keySet()) {
			if (callback != null)
				callback.onCreateCallback(newArea);
		}

		return newArea;
	}

	@Transactional
	@Override
	public Area getRootArea(int organizationId) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			return (Area) em.createQuery("FROM Area a WHERE a.organization.id = ? AND a.parent IS NULL").setParameter(
					1, organizationId).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Transactional
	@Override
	public void removeArea(int organizationId, int areaId) throws AreaNotFoundException, UndeletableAreaException {
		EntityManager em = entityManagerService.getEntityManager();

		Area area = getArea(organizationId, areaId, em);

		if (area.getParent() == null)
			throw new UndeletableAreaException();

		// node means all sub areas including parent
		for (Area node : getSubAreas(organizationId, areaId)) {
			for (AreaEventCallback callback : callbacks.keySet()) {
				if (callback != null)
					callback.onRemoveCallback(node);
			}
		}

		em.remove(area);
	}

	@Transactional
	@Override
	public Area updateArea(int organizationId, int areaId, String name, String description)
			throws AreaNotFoundException {
		EntityManager em = entityManagerService.getEntityManager();

		Area area = getArea(organizationId, areaId, em);
		area.setName(name);
		area.setDescription(description);
		em.merge(area);

		for (AreaEventCallback callback : callbacks.keySet()) {
			if (callback != null)
				callback.onUpdateCallback(area);
		}

		return area;
	}

	@Override
	public void registerCallback(AreaEventCallback callback) {
		callbacks.put(callback, null);
	}

	@Override
	public void unregisterCallback(AreaEventCallback callback) {
		callbacks.remove(callback);
	}

	private Area getArea(int organizationId, int areaId, EntityManager em) throws AreaNotFoundException {
		Area area = em.find(Area.class, areaId);
		if (area == null || area.getOrganization().getId() != organizationId)
			throw new AreaNotFoundException(areaId);
		return area;
	}

}
