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
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.dom.api.HostApi;
import org.krakenapps.dom.model.Host;
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
	public void getHosts(Request req, Response resp) {
		Collection<Host> hosts = hostApi.getHosts(req.getOrgDomain());
		resp.put("hosts", PrimitiveConverter.serialize(hosts));
	}

	@MsgbusMethod
	public void getHost(Request req, Response resp) {
		String guid = req.getString("guid");
		Host host = hostApi.getHost(req.getOrgDomain(), guid);
		resp.put("host", PrimitiveConverter.serialize(host));
	}

	@MsgbusMethod
	public void createHost(Request req, Response resp) {
		Host host = (Host) PrimitiveConverter.overwrite(new Host(), req.getParams());
		hostApi.createHost(req.getOrgDomain(), host);
		resp.put("guid", host.getGuid());
	}

	@MsgbusMethod
	public void updateHost(Request req, Response resp) {
		Host before = hostApi.getHost(req.getOrgDomain(), req.getString("guid"));
		Host host = (Host) PrimitiveConverter.overwrite(before, req.getParams());
		hostApi.updateHost(req.getOrgDomain(), host);
	}

	@MsgbusMethod
	public void removeHost(Request req, Response resp) {
		String guid = req.getString("guid");
		hostApi.removeHost(req.getOrgDomain(), guid);
	}
}
