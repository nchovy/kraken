/*
 * Copyright 2010 NCHOVY
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
 package org.krakenapps.sonar.metabase;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.sonar.Metabase;
import org.krakenapps.sonar.metabase.model.Application;
import org.krakenapps.sonar.metabase.model.Environment;
import org.krakenapps.sonar.metabase.model.AttackLog;
import org.krakenapps.sonar.metabase.model.IpEndPoint;
import org.krakenapps.sonar.metabase.model.Vendor;

@Component(name = "sonar-metabase")
@Provides
@JpaConfig(factory = "sonar")
public class PersistentMetabase implements Metabase {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<IpEndPoint> getIpEndPoints() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM IpEndPoint").getResultList();
	}

	@Transactional
	@Override
	public IpEndPoint getIpEndPoint(MacAddress mac) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			IpEndPoint old = (IpEndPoint) em.createQuery("FROM IpEndPoint ep WHERE ep.mac = ?").setParameter(1,
					mac.toString()).getSingleResult();
			return old;
		} catch (NoResultException e) {
			return null;
		}
	}

	@Transactional
	@Override
	public IpEndPoint updateIpEndPoint(MacAddress mac, InetAddress ip) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			IpEndPoint old = (IpEndPoint) em.createQuery("FROM IpEndPoint ep WHERE ep.mac = ?").setParameter(1,
					mac.toString()).getSingleResult();
			old.setIp(ip);
			em.merge(old);
			return old;
		} catch (NoResultException e) {
			IpEndPoint ep = new IpEndPoint(mac);
			ep.setIp(ip);
			em.persist(ep);
			return ep;
		}
	}

	@Transactional
	@Override
	public IpEndPoint updateIpEndpoint(MacAddress mac, Environment environment) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			IpEndPoint old = (IpEndPoint) em.createQuery("FROM IpEndPoint ep WHERE ep.mac = ?").setParameter(1,
					mac.toString()).getSingleResult();
			old.setEnvironment(environment);
			em.merge(old);
			return old;
		} catch (NoResultException e) {
			IpEndPoint ep = new IpEndPoint(mac);
			ep.setEnvironment(environment);
			em.persist(ep);
			return ep;
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public void clearIpEndpoints() {
		EntityManager em = entityManagerService.getEntityManager();
		Collection<IpEndPoint> endpoints = em.createQuery("FROM IpEndPoint").getResultList();
		for (IpEndPoint ep : endpoints)
			em.remove(ep);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<Vendor> getVendors() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM Vendor").getResultList();
	}

	@Transactional
	@Override
	public Vendor getVendor(String name) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			Vendor old = (Vendor) em.createQuery("FROM Vendor v WHERE v.name = ?").setParameter(1,
					name).getSingleResult();
			return old;
		} catch (NoResultException e) {
			return null;
		}
	}

	@Transactional
	@Override
	public Vendor updateVendor(String name) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			Vendor old = (Vendor) em.createQuery("FROM Vendor v WHERE v.name = ?").setParameter(1,
					name).getSingleResult();
			return old;
		} catch (NoResultException e) {
			Vendor vendor = new Vendor(name);
			em.persist(vendor);
			return vendor;
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public void clearVendors() {
		EntityManager em = entityManagerService.getEntityManager();
		Collection<Vendor> vendors = em.createQuery("FROM Vendor").getResultList();
		for (Vendor vendor : vendors)
			em.remove(vendor);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<Environment> getEnvironments() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM Environment").getResultList();
	}

	@Override
	public Environment getEnvironment(Vendor vendor, String family,
			String description) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			Environment old = (Environment) em.createQuery(
					"FROM Environment e WHERE e.vendor = ? AND e.family = ? AND e.description = ?")
					.setParameter(1, vendor)
					.setParameter(2, family)
					.setParameter(3, description).getSingleResult();
			return old;
		} catch (NoResultException e) {
			return null;
		}
	}

	@Transactional
	@Override
	public Environment updateEnvironment(Vendor vendor, String family,
			String description) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			Environment old = (Environment) em.createQuery(
					"FROM Environment e WHERE e.vendor = ? AND e.family = ? AND e.description = ?")
					.setParameter(1, vendor)
					.setParameter(2, family)
					.setParameter(3, description).getSingleResult();
			return old;
		} catch (NoResultException e) {
			Environment environment = new Environment(vendor, family, description);
			em.persist(environment);
			return environment;
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public void clearEnvironments() {
		EntityManager em = entityManagerService.getEntityManager();
		Collection<Environment> environments = em.createQuery("FROM Environment").getResultList();
		for (Environment env : environments)
			em.remove(env);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public void clearApplications() {
		EntityManager em = entityManagerService.getEntityManager();
		Collection<Application> apps = em.createQuery("FROM Application").getResultList();
		for (Application app : apps)
			em.remove(app);
	}

	@Override
	public Application getApplication(Vendor vendor, String name) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			Application old = (Application) em.createQuery(
					"FROM Application a WHERE a.vendor = ? AND a.name = ?")
					.setParameter(1, vendor)
					.setParameter(2, name).getSingleResult();
			return old;
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<Application> getApplications() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM Application").getResultList();
	}

	@Transactional
	@Override
	public void alert(AttackLog log) {
		EntityManager em = entityManagerService.getEntityManager();
		em.persist(log);
	}

	@Transactional
	@Override
	public void clearIdsLog() {
		EntityManager em = entityManagerService.getEntityManager();
		em.createQuery("DELETE FROM IdsLog").executeUpdate();
	}

	@Transactional
	@Override
	public Application updateApplication(Vendor vendor, String name,
			String version,
			IpEndPoint endpoint) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			Application old = (Application) em.createQuery(
					"FROM Application a WHERE a.vendor = ? AND a.name = ?")
					.setParameter(1, vendor)
					.setParameter(2, name).getSingleResult();
			old.setVersion(version);

			boolean found = false;
			for (IpEndPoint ip : old.getEndpoints()) {
				if (ip.getId() == endpoint.getId()) {
					found = true;
					break;
				}
			}
			if (!found) {
				old.AddEndpoint(endpoint);
			}
			return old;
		} catch (NoResultException e) {
			Application application = new Application(vendor, name, version, endpoint);
			em.persist(application);
			return application;
		}
	}

	@Transactional
	@Override
	public IpEndPoint updateIpEndPoint(InetSocketAddress localAddress) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			IpEndPoint old = (IpEndPoint) em.createQuery("FROM IpEndPoint ep WHERE ep.ip = ?").setParameter(1,
					localAddress.toString()).getSingleResult();
			em.merge(old);
			return old;
		} catch (NoResultException e) {
			IpEndPoint ep = new IpEndPoint(localAddress);
			em.persist(ep);
			return ep;
		}
	}
}