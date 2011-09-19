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
import org.krakenapps.dom.api.AdminSettingApi;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-admin-setting-plugin")
@MsgbusPlugin
public class AdminSettingPlugin {
	@Requires
	private AdminSettingApi adminSettingApi;

	@MsgbusMethod
	public void getAdminSetting(Request req, Response resp) {
		int organizationId = req.getOrgId();
		int adminId = req.getSession().getAdminId();
		String name = req.getString("name");
		String value = adminSettingApi.getAdminSetting(organizationId, adminId, name);
		resp.put("setting", value);
	}

	@MsgbusMethod
	public void updateAdminSetting(Request req, Response resp) {
		int organizationId = req.getOrgId();
		int adminId = req.getSession().getAdminId();
		String name = req.getString("name");
		String value = req.getString("value");

		adminSettingApi.setAdminSetting(organizationId, adminId, name, value);
	}

	@MsgbusMethod
	public void removeAdminSetting(Request req, Response resp) {
		int organizationId = req.getOrgId();
		int adminId = req.getSession().getAdminId();
		String name = req.getString("name");

		adminSettingApi.unsetAdminSetting(organizationId, adminId, name);
	}
}
