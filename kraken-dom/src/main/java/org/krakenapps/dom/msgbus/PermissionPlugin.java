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

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.PermissionApi;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-permission-plugin")
@MsgbusPlugin
@JpaConfig(factory = "dom")
public class PermissionPlugin {
	@Requires
	private PermissionApi permApi;

	@MsgbusMethod
	public void getPermissions(Request req, Response resp) {
		int orgId = req.getSession().getOrgId();
		int adminId = req.getSession().getAdminId();
		String group = req.getString("group");

		resp.put("permissions", Marshaler.marshal(permApi.getPermissions(orgId, adminId, group)));
	}
}
