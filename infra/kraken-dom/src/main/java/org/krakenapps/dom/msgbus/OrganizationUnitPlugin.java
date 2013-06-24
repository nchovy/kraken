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
import java.util.Collection;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.model.OrganizationUnit;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPermission;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-org-unit-plugin")
@MsgbusPlugin
public class OrganizationUnitPlugin {
	@Requires
	private ConfigManager conf;

	@Requires
	private OrganizationUnitApi orgUnitApi;

	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "admin_grant")
	public void removeAllOrganizationUnits(Request req, Response resp) {
		String domain = req.getOrgDomain();
		List<String> guids = new ArrayList<String>();
		for (OrganizationUnit u : orgUnitApi.getOrganizationUnits(domain)) {
			guids.add(u.getGuid());
		}

		orgUnitApi.removeOrganizationUnits(domain, guids, true);
	}

	@MsgbusMethod
	public void getOrganizationUnits(Request req, Response resp) {
		boolean includeChildren = true;
		if (req.has("include_children"))
			includeChildren = req.getBoolean("include_children");
		Collection<OrganizationUnit> orgUnits = orgUnitApi.getOrganizationUnits(req.getOrgDomain(), includeChildren);
		resp.put("org_units", PrimitiveConverter.serialize(orgUnits));
	}

	@MsgbusMethod
	public void getOrganizationUnit(Request req, Response resp) {
		String guid = req.getString("guid");
		OrganizationUnit orgUnit = orgUnitApi.getOrganizationUnit(req.getOrgDomain(), guid);
		resp.put("org_unit", PrimitiveConverter.serialize(orgUnit));
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "admin_grant")
	public void createOrganizationUnit(Request req, Response resp) {
		OrganizationUnit orgUnit = (OrganizationUnit) PrimitiveConverter.overwrite(new OrganizationUnit(), req.getParams(),
				conf.getParseCallback(req.getOrgDomain()));
		orgUnitApi.createOrganizationUnit(req.getOrgDomain(), orgUnit);
		resp.put("guid", orgUnit.getGuid());
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "admin_grant")
	public void updateOrganizationUnit(Request req, Response resp) {
		OrganizationUnit orgUnit = (OrganizationUnit) PrimitiveConverter.overwrite(new OrganizationUnit(), req.getParams(),
				conf.getParseCallback(req.getOrgDomain()));
		orgUnitApi.updateOrganizationUnit(req.getOrgDomain(), orgUnit);
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "admin_grant")
	public void removeOrganizationUnit(Request req, Response resp) {
		String guid = req.getString("guid");
		orgUnitApi.removeOrganizationUnit(req.getOrgDomain(), guid);
	}
}
