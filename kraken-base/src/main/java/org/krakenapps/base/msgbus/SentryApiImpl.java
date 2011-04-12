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
package org.krakenapps.base.msgbus;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.base.SentryProxy;
import org.krakenapps.base.SentryProxyRegistry;
import org.krakenapps.base.msgbus.exception.SentryAlreadyExistsException;
import org.krakenapps.base.msgbus.exception.SentryNotFoundException;
import org.krakenapps.dom.api.HostApi;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.exception.HostNotFoundException;
import org.krakenapps.dom.model.Host;
import org.krakenapps.dom.model.HostExtension;
import org.krakenapps.dom.model.Sentry;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "sentriy-api")
@Provides
@JpaConfig(factory = "dom")
public class SentryApiImpl extends AbstractApi<Sentry> implements SentryApi {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;
	@Requires
	private HostApi hostApi;
	@Requires
	private SentryProxyRegistry sentryProxyRegistry;

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<Sentry> getSentries(int organizationId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM Sentry s WHERE s.organization.id = ?").setParameter(1, organizationId)
				.getResultList();
	}

	@Transactional
	@Override
	public Sentry getSentry(int organizationId, int hostId) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			return (Sentry) em.createQuery("FROM Sentry s WHERE s.organization.id = ? AND s.host.id = ?").setParameter(
					1, organizationId).setParameter(2, hostId).getSingleResult();
		} catch (NoResultException e) {
			throw new SentryNotFoundException();
		}
	}

	@Transactional
	@Override
	public Sentry getSentry(String guid) {
		EntityManager em = entityManagerService.getEntityManager();
		try {
			return (Sentry) em.createQuery("FROM Sentry s WHERE s.guid = ?").setParameter(1, guid).getSingleResult();
		} catch (NoResultException e) {
		}
		return null;
	}

	@Override
	public int createSentry(int organizationId, int hostId) {
		Sentry sentry = createSentryInternal(organizationId, hostId);
		fireEntityAdded(sentry);
		return sentry.getHost().getId();
	}

	@Transactional
	private Sentry createSentryInternal(int organizationId, int hostId) {
		EntityManager em = entityManagerService.getEntityManager();

		Host host = hostApi.getHost(organizationId, hostId);
		if (host == null)
			throw new HostNotFoundException();

		if (host.getSentry() != null)
			throw new SentryAlreadyExistsException();

		Sentry sentry = new Sentry();
		sentry.setOrganization(host.getOrganization());
		sentry.setHost(host);
		sentry.setConnected(false);

		host.setSentry(sentry);

		em.persist(sentry);
		em.merge(host);

		// add extension
		addExtension(host, "Nchovy.WatchCat.Plugins.Core.HostConfig.LoggerTab");
		addExtension(host, "Nchovy.WatchCat.Plugins.Core.HostConfig.PerformanceTab");
		addExtension(host, "Nchovy.WatchCat.Plugins.Core.HostConfig.ProcessTab");
		addExtension(host, "Nchovy.WatchCat.Plugins.Core.HostConfig.NetstatTab");
		addExtension(host, "Nchovy.WatchCat.Plugins.Core.HostConfig.RouteTab");

		return sentry;
	}

	private void addExtension(Host host, String className) {
		HostExtension extension = hostApi.getHostExtension("Nchovy.WatchCat.Plugins.Core.HostConfig.SentryTab");
		if (extension != null)
			host.getExtensions().add(extension);
	}

	@Override
	public void updateSentry(int organizationId, int hostId, boolean isConnected) {
		Sentry sentry = updateSentryInternal(organizationId, hostId, isConnected);
		fireEntityUpdated(sentry);
	}

	@Transactional
	private Sentry updateSentryInternal(int organizationId, int hostId, boolean isConnected) {
		EntityManager em = entityManagerService.getEntityManager();
		Host host = hostApi.getHost(organizationId, hostId);
		if (host == null)
			throw new HostNotFoundException();

		if (host.getSentry() == null)
			throw new SentryNotFoundException();

		host.getSentry().setConnected(isConnected);

		em.merge(host);

		return host.getSentry();
	}

	@Override
	public void removeSentry(int organizationId, int hostId) {
		Sentry sentry = removeSentryInternal(organizationId, hostId);
		fireEntityRemoved(sentry);
	}

	@Transactional
	private Sentry removeSentryInternal(int organizationId, int hostId) {
		EntityManager em = entityManagerService.getEntityManager();
		Host host = hostApi.getHost(organizationId, hostId);
		if (host == null)
			throw new HostNotFoundException();

		if (host.getSentry() == null)
			throw new SentryNotFoundException();

		Sentry sentry = host.getSentry();
		host.setSentry(null);

		em.remove(sentry);
		em.merge(host);

		return sentry;
	}

	@Override
	public void disconnectSentry(int organizationId, int hostId) {
		Host host = hostApi.getHost(organizationId, hostId);
		if (host == null)
			return;

		SentryProxy proxy = sentryProxyRegistry.getSentry(host.getGuid());
		if (proxy != null && proxy.isOpen()) {
			proxy.close();
		}
	}
}
