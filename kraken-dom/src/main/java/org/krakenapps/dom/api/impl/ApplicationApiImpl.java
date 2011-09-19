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
import java.util.Map;
import java.util.UUID;

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
import org.krakenapps.dom.model.ApplicationMetadata;
import org.krakenapps.dom.model.ApplicationMetadataKey;
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

	@Override
	public Vendor createVendor(String name) {
		Vendor vendor = createVendorInternal(name);
		return vendor;
	}

	@Transactional
	private Vendor createVendorInternal(String name) {
		EntityManager em = entityManagerService.getEntityManager();
		Vendor vendor = new Vendor();
		vendor.setGuid(UUID.randomUUID().toString());
		vendor.setName(name);
		vendor.setCreateDateTime(new Date());
		vendor.setUpdateDateTime(new Date());

		em.persist(vendor);
		return vendor;
	}

	@Override
	public Vendor updateVendor(String guid, String name) {
		Vendor vendor = updateVendorInternal(guid, name);
		return vendor;
	}

	@Transactional
	private Vendor updateVendorInternal(String guid, String name) {
		EntityManager em = entityManagerService.getEntityManager();

		Vendor vendor = em.find(Vendor.class, guid);
		if (vendor == null)
			throw new VendorNotFoundException();

		vendor.setName(name);
		vendor.setUpdateDateTime(new Date());

		em.merge(vendor);

		return vendor;
	}

	@Override
	public Vendor removeVendor(String guid) {
		Vendor vendor = removeVendorInternal(guid);
		return vendor;
	}

	@Transactional
	private Vendor removeVendorInternal(String guid) {
		EntityManager em = entityManagerService.getEntityManager();
		Vendor vendor = em.find(Vendor.class, guid);

		if (vendor == null)
			throw new VendorNotFoundException();

		em.remove(vendor);

		return vendor;
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
	public Application createApplication(String name, String platform, Map<String, String> props) {
		return createApplication(null, name, platform, props);
	}

	@Override
	public Application createApplication(String vendorGuid, String name, String platform, Map<String, String> props) {
		Application app = createApplicationInternal(vendorGuid, name, platform, props);
		fireEntityAdded(app);
		return app;
	}

	@Transactional
	private Application createApplicationInternal(String vendorGuid, String name, String platform,
			Map<String, String> props) {
		EntityManager em = entityManagerService.getEntityManager();

		Vendor vendor = null;
		if (vendorGuid != null)
			vendor = getVendor(vendorGuid);

		Application app = new Application();
		app.setGuid(UUID.randomUUID().toString());
		app.setVendor(vendor);
		app.setName(name);
		app.setPlatform(platform);
		app.setCreateDateTime(new Date());
		app.setUpdateDateTime(new Date());

		em.persist(app);

		setAppMetadatas(em, app, props);

		return app;
	}

	@Override
	public Application updateApplication(String guid, String name, Map<String, String> props) {
		Application app = updateApplicationInternal(guid, name, props);
		fireEntityUpdated(app);
		return app;
	}

	@Transactional
	private Application updateApplicationInternal(String guid, String name, Map<String, String> props) {
		EntityManager em = entityManagerService.getEntityManager();
		Application app = em.find(Application.class, guid);
		if (app == null)
			throw new ApplicationNotFoundException();

		app.setName(name);
		app.setUpdateDateTime(new Date());

		em.merge(app);

		for (ApplicationMetadata d : app.getMetadatas()) {
			em.remove(d);
		}

		app.getMetadatas().clear();
		setAppMetadatas(em, app, props);

		em.merge(app);

		return app;
	}

	private void setAppMetadatas(EntityManager em, Application app, Map<String, String> props) {
		for (String propName : props.keySet()) {
			ApplicationMetadata m = new ApplicationMetadata();
			ApplicationMetadataKey key = new ApplicationMetadataKey();
			key.setApplication(app);
			key.setName(propName);
			m.setKey(key);
			m.setValue(props.get(propName));

			em.persist(m);
		}
	}

	@Override
	public Application removeApplication(String guid) {
		Application app = removeApplicationInternal(guid);
		fireEntityRemoved(app);
		return app;
	}

	@Transactional
	private Application removeApplicationInternal(String guid) {
		EntityManager em = entityManagerService.getEntityManager();
		Application app = em.find(Application.class, guid);
		if (app == null)
			throw new ApplicationNotFoundException();

		em.remove(app);

		return app;
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

	@Override
	public ApplicationVersion createApplicationVersion(String vendorName, String appName, String version) {
		ApplicationVersion appVersion = createApplicationVersionIntenal(vendorName, appName, version);
		return appVersion;
	}

	@Transactional
	private ApplicationVersion createApplicationVersionIntenal(String vendorName, String appName, String version) {
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

	@Override
	public ApplicationVersion updateApplicationVersion(String guid, String version) {
		ApplicationVersion appVersion = updateApplicationVersionInternal(guid, version);
		return appVersion;
	}

	@Transactional
	private ApplicationVersion updateApplicationVersionInternal(String guid, String version) {
		EntityManager em = entityManagerService.getEntityManager();
		ApplicationVersion appVersion = em.find(ApplicationVersion.class, guid);
		if (appVersion == null)
			throw new ApplicationVersionNotFoundException();

		appVersion.setVersion(version);
		appVersion.setUpdateDateTime(new Date());

		em.merge(appVersion);

		return appVersion;
	}

	@Override
	public ApplicationVersion removeApplicationVersion(String guid) {
		ApplicationVersion appVersion = removeApplicationVersionInternal(guid);
		return appVersion;
	}

	@Transactional
	private ApplicationVersion removeApplicationVersionInternal(String guid) {
		EntityManager em = entityManagerService.getEntityManager();
		ApplicationVersion version = em.find(ApplicationVersion.class, guid);
		if (version == null)
			return null;

		em.remove(version);

		return version;
	}
}
