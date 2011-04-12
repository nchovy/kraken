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
package org.krakenapps.dom.msgbus;

import java.util.Collection;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.NetworkAddressApi;
import org.krakenapps.dom.model.NetworkAddress;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-network-address-plugin")
@MsgbusPlugin
public class NetworkAddressPlugin {

	@Requires
	private NetworkAddressApi networkAddressApi;

	@MsgbusMethod
	public void getNetworkAddresses(Request req, Response resp) {
		int organizationId = req.getOrgId();
		Collection<NetworkAddress> addrs = networkAddressApi.getNetworkAddresses(organizationId);
		resp.put("addrs", Marshaler.marshal(addrs));
	}

	@MsgbusMethod
	public void getNetworkAddress(Request req, Response resp) {
		int organizationId = req.getSession().getOrgId();
		int id = req.getInteger("id");
		NetworkAddress addr = networkAddressApi.getNetworkAddress(organizationId, id);
		if (addr != null)
			resp.put("addr", addr.marshal());
		else
			resp.put("addr", null);
	}

	@MsgbusMethod
	public void createNetworkAddress(Request req, Response resp) {
		try {
			int organizationId = req.getSession().getOrgId();
			String name = req.getString("name");
			int type = req.getInteger("type");
			String address1 = req.getString("address1");
			String address2 = req.getString("address2");

			networkAddressApi.createNetworkAddress(organizationId, name, type, address1, address2);
		} catch (IllegalStateException e) {
			if (e.getMessage().startsWith("organization"))
				throw new MsgbusException("dom", "org-not-found");

			throw e;
		}
	}

	@MsgbusMethod
	public void updateNetworkAddress(Request req, Response resp) {
		try {
			int organizationId = req.getSession().getOrgId();
			int id = req.getInteger("id");
			String name = req.getString("name");
			int type = req.getInteger("type");
			String address1 = req.getString("address1");
			String address2 = req.getString("address2");

			networkAddressApi.updateNetworkAddress(organizationId, id, name, type, address1, address2);
		} catch (IllegalStateException e) {
			if (e.getMessage().startsWith("organization"))
				throw new MsgbusException("dom", "org-not-found");
			if (e.getMessage().startsWith("network"))
				throw new MsgbusException("dom", "network-address-not-found");
			throw e;
		}
	}

	@MsgbusMethod
	public void removeNetworkAddress(Request req, Response resp) {
		try {
			int organizationId = req.getSession().getOrgId();
			int id = req.getInteger("id");

			networkAddressApi.removeNetworkAddress(organizationId, id);
		} catch (IllegalStateException e) {
			if (e.getMessage().startsWith("network"))
				throw new MsgbusException("dom", "network-address-not-found");
			throw e;
		}
	}
}
