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
import org.krakenapps.dom.api.NetworkAddressApi;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.model.NetworkAddress;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "dom-network-address-api")
@Provides
@JpaConfig(factory = "dom")
public class NetworkAddressApiImpl extends AbstractApi<NetworkAddress> implements NetworkAddressApi {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Requires
	private OrganizationApi orgApi;

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<NetworkAddress> getNetworkAddresses(int organizationId) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM NetworkAddress a WHERE a.organization.id = ?").setParameter(1, organizationId)
				.getResultList();
	}

	@Transactional
	@Override
	public NetworkAddress getNetworkAddress(int organizationId, int id) {
		try {
			EntityManager em = entityManagerService.getEntityManager();
			return (NetworkAddress) em.createQuery("FROM NetworkAddress a WHERE a.organization.id = ? AND a.id = ?")
					.setParameter(1, organizationId).setParameter(2, id).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public NetworkAddress createNetworkAddress(int organizationId, String name, int type, String address1,
			String address2) {
		NetworkAddress addr = createNetworkAddressInternal(organizationId, name, type, address1, address2);
		fireEntityAdded(addr);
		return addr;
	}

	@Transactional
	private NetworkAddress createNetworkAddressInternal(int organizationId, String name, int type, String address1,
			String address2) {
		EntityManager em = entityManagerService.getEntityManager();

		Organization org = orgApi.getOrganization(organizationId);
		if (org == null)
			throw new IllegalStateException("organization not found: " + organizationId);

		NetworkAddress addr = new NetworkAddress();
		addr.setOrganization(org);
		addr.setName(name);
		addr.setType(type);
		addr.setAddress1(address1);
		addr.setAddress2(address2);
		addr.setCreateDateTime(new Date());
		addr.setUpdateDateTime(new Date());

		em.persist(addr);
		return addr;
	}

	@Override
	public NetworkAddress updateNetworkAddress(int organizationId, int id, String name, int type, String address1,
			String address2) {
		NetworkAddress addr = updateNetworkAddressInternal(organizationId, id, name, type, address1, address2);
		fireEntityUpdated(addr);
		return addr;
	}

	@Transactional
	private NetworkAddress updateNetworkAddressInternal(int organizationId, int id, String name, int type,
			String address1, String address2) {
		EntityManager em = entityManagerService.getEntityManager();

		NetworkAddress addr = getNetworkAddress(organizationId, id);
		if (addr == null)
			throw new IllegalStateException("network address not found: " + organizationId + "," + id);

		addr.setName(name);
		addr.setType(type);
		addr.setAddress1(address1);
		addr.setAddress2(address2);
		addr.setUpdateDateTime(new Date());

		em.merge(addr);
		return addr;
	}

	@Override
	public NetworkAddress removeNetworkAddress(int organizationId, int id) {
		NetworkAddress addr = removeNetworkAddressInternal(organizationId, id);
		fireEntityRemoved(addr);
		return addr;
	}

	@Transactional
	private NetworkAddress removeNetworkAddressInternal(int organizationId, int id) {
		EntityManager em = entityManagerService.getEntityManager();

		NetworkAddress addr = getNetworkAddress(organizationId, id);
		if (addr == null)
			throw new IllegalStateException("network address not found: " + organizationId + "," + id);

		em.remove(addr);
		return addr;
	}
}
