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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.HostApi;
import org.krakenapps.dom.exception.HostNotFoundException;
import org.krakenapps.dom.model.Host;
import org.krakenapps.dom.model.HostExtension;
import org.krakenapps.dom.model.HostType;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-host-plugin")
@MsgbusPlugin
public class HostPlugin {
	@Requires
	private HostApi hostApi;

	@MsgbusMethod
	public void getHostTypes(Request req, Response resp) {
		List<HostType> hostTypes = hostApi.getHostTypes();
		resp.put("host_types", Marshaler.marshal(hostTypes));
	}

	@MsgbusMethod
	public void getHostExtensions(Request req, Response resp) {
		int orgId = req.getOrgId();
		int hostId = req.getInteger("host_id");
		Host host = hostApi.getHost(orgId, hostId);
		if (host == null)
			throw new HostNotFoundException();

		List<HostExtension> extensions = new ArrayList<HostExtension>(host.getExtensions());
		Collections.sort(extensions, new Comparator<HostExtension>() {
			@Override
			public int compare(HostExtension o1, HostExtension o2) {
				return o1.getOrdinal() - o2.getOrdinal();
			}
		});

		resp.put("extensions", Marshaler.marshal(extensions));
	}

	@MsgbusMethod
	public void getHosts(Request req, Response resp) {
		int organizationId = req.getOrgId();
		List<Host> hosts = hostApi.getHosts(organizationId);
		resp.put("hosts", Marshaler.marshal(hosts));
	}

	@MsgbusMethod
	public void getHost(Request req, Response resp) {
		Host host = hostApi.getHost(req.getOrgId(), req.getInteger("host_id"));
		if (host == null)
			throw new HostNotFoundException();

		resp.put("host", host.marshal());
	}

	@MsgbusMethod
	public void createHost(Request req, Response resp) {
		int organizationId = req.getOrgId();
		int hostTypeId = req.getInteger("host_type_id");
		int areaId = req.getInteger("area_id");
		String name = req.getString("name");
		String description = req.getString("description");

		Host host = hostApi.createHost(organizationId, hostTypeId, areaId, name, description);
		resp.put("id", host.getId());
	}

	@MsgbusMethod
	public void updateHost(Request req, Response resp) {
		int organizationId = req.getOrgId();
		int hostId = req.getInteger("host_id");
		String name = req.getString("name");
		String description = req.getString("description");

		hostApi.updateHost(organizationId, hostId, name, description);
	}

	@MsgbusMethod
	public void updateHostGuid(Request req, Response resp) {
		int hostId = req.getInteger("host_id");
		String guid = req.getString("host_guid");
		hostApi.updateHostGuid(req.getOrgId(), hostId, guid);
	}

	@MsgbusMethod
	public void removeHost(Request req, Response resp) {
		int organizationId = req.getOrgId();
		int hostId = req.getInteger("host_id");
		hostApi.removeHost(organizationId, hostId);
	}

	@MsgbusMethod
	public void moveHost(Request req, Response resp) {
		int organizationId = req.getOrgId();
		int hostId = req.getInteger("host_id");
		int areaId = req.getInteger("area_id");

		hostApi.moveHost(organizationId, hostId, areaId);
	}

	@MsgbusMethod
	public void mapHostExtensions(Request req, Response resp) {
		int hostId = req.getInteger("host_id");
		Set<String> hostExtensionNames = parseHostExtensionNames(req);
		hostApi.mapHostExtensions(req.getOrgId(), hostId, hostExtensionNames);
	}

	@MsgbusMethod
	public void unmapHostExtensions(Request req, Response resp) {
		int hostId = req.getInteger("host_id");
		Set<String> hostExtensionNames = parseHostExtensionNames(req);
		hostApi.unmapHostExtensions(req.getOrgId(), hostId, hostExtensionNames);
	}

	@SuppressWarnings("unchecked")
	private Set<String> parseHostExtensionNames(Request req) {
		Set<String> names = new HashSet<String>();
		List<String> array = (List<String>) req.get("extensions");
		for (String className : array) {
			names.add(className);
		}

		return names;
	}
}
