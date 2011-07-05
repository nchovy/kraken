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

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.ApplicationApi;
import org.krakenapps.dom.exception.ApplicationNotFoundException;
import org.krakenapps.dom.exception.ApplicationVersionNotFoundException;
import org.krakenapps.dom.exception.VendorNotFoundException;
import org.krakenapps.dom.model.Application;
import org.krakenapps.dom.model.ApplicationVersion;
import org.krakenapps.dom.model.Vendor;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "dom-app-api")
@Provides
@JpaConfig(factory = "dom")
public class ApplicationApiImpl extends AbstractApi<Application> implements ApplicationApi {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<Vendor> getVendors() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM Vendor").getResultList();
	}

	@Transactional
	@Override
	public Vendor getVendor(String guid) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.find(Vendor.class, guid);
	}

	@Transactional
	@Override
	public Vendor createVendor(String name) {
		EntityManager em = entityManagerService.getEntityManager();
		Vendor vendor = new Vendor();
		vendor.setName(name);
		vendor.setCreateDateTime(new Date());
		vendor.setUpdateDateTime(new Date());

		em.persist(vendor);
		return vendor;
	}

	@Transactional
	@Override
	public void updateVendor(String guid, String name) {
		EntityManager em = entityManagerService.getEntityManager();

		Vendor vendor = em.find(Vendor.class, guid);
		if (vendor == null)
			throw new VendorNotFoundException();

		vendor.setName(name);
		vendor.setUpdateDateTime(new Date());

		em.merge(vendor);
	}

	@Transactional
	@Override
	public void removeVendor(String guid) {
		EntityManager em = entityManagerService.getEntityManager();
		Vendor vendor = em.find(Vendor.class, guid);

		if (vendor == null)
			throw new VendorNotFoundException();

		em.remove(vendor);
	}

	@Override
	public Collection<Application> getApplications() {
		return getApplications(null);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<Application> getApplications(String vendorGuid) {
		EntityManager em = entityManagerService.getEntityManager();
		if (vendorGuid == null)
			return em.createQuery("FROM Application a").getResultList();

		return em.createQuery("FROM Application a WHERE a.vendor.guid = ?").setParameter(1, vendorGuid).getResultList();
	}

	@Transactional
	@Override
	public Application getApplication(String guid) {
		EntityManager em = entityManagerService.getEntityManager();
		if (guid == null)
			return null;

		Application app = em.find(Application.class, guid);
		if (app == null)
			return null;
		
		// force loading
		app.getMetadatas().size();
		app.getVersions().size();
		
		return app;
	}

	@Transactional
	@Override
	public Application getApplication(String vendorName, String name) {
		EntityManager em = entityManagerService.getEntityManager();

		try {
			return (Application) em.createQuery("FROM Application a JOIN a.vendors v WHERE a.name = ? and v.name = ?")
					.setParameter(1, name).setParameter(2, vendorName).getSingleResult();
		} catch (NoResultException e) {
		}

		return null;
	}

	@Override
	public Application createApplication(String name) {
		return createApplication(null, name);
	}

	@Transactional
	@Override
	public Application createApplication(String vendorGuid, String name) {
		EntityManager em = entityManagerService.getEntityManager();

		Vendor vendor = null;
		if (vendorGuid != null)
			vendor = getVendor(vendorGuid);

		Application app = new Application();
		app.setVendor(vendor);
		app.setName(name);
		app.setCreateDateTime(new Date());
		app.setUpdateDateTime(new Date());

		em.persist(app);

		return app;
	}

	@Transactional
	@Override
	public void updateApplication(String guid, String name) {
		EntityManager em = entityManagerService.getEntityManager();
		Application app = em.find(Application.class, guid);
		if (app == null)
			throw new ApplicationNotFoundException();

		app.setName(name);
		app.setUpdateDateTime(new Date());

		em.merge(app);
	}

	@Transactional
	@Override
	public void removeApplication(String guid) {
		EntityManager em = entityManagerService.getEntityManager();
		Application app = em.find(Application.class, guid);
		if (app == null)
			throw new ApplicationNotFoundException();

		em.remove(app);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<ApplicationVersion> getApplicationVersions(String vendorName, String appName) {
		EntityManager em = entityManagerService.getEntityManager();
		Application app = getApplication(vendorName, appName);
		if (app == null)
			throw new ApplicationNotFoundException();

		return (Collection<ApplicationVersion>) em
				.createQuery("FROM ApplicationVersion v WHERE v.application.guid = ?").setParameter(1, app.getGuid());
	}

	@Transactional
	@Override
	public ApplicationVersion createApplicationVersion(String vendorName, String appName, String version) {
		EntityManager em = entityManagerService.getEntityManager();
		Vendor vendor = getVendor(vendorName);
		if (vendor == null)
			throw new VendorNotFoundException();

		Application app = getApplication(vendorName, appName);
		if (app == null)
			throw new ApplicationNotFoundException();

		ApplicationVersion appVersion = new ApplicationVersion();
		appVersion.setApplication(app);
		appVersion.setVersion(version);
		appVersion.setCreateDateTime(new Date());
		appVersion.setUpdateDateTime(new Date());

		em.persist(appVersion);

		return appVersion;
	}

	@Transactional
	@Override
	public void updateApplicationVersion(String guid, String version) {
		EntityManager em = entityManagerService.getEntityManager();
		ApplicationVersion appVersion = em.find(ApplicationVersion.class, guid);
		if (appVersion == null)
			throw new ApplicationVersionNotFoundException();

		appVersion.setVersion(version);
		appVersion.setUpdateDateTime(new Date());

		em.merge(appVersion);
	}

	@Transactional
	@Override
	public void removeApplicationVersion(String guid) {
		EntityManager em = entityManagerService.getEntityManager();
		ApplicationVersion version = em.find(ApplicationVersion.class, guid);
		if (version == null)
			return;

		em.remove(version);
	}

}
