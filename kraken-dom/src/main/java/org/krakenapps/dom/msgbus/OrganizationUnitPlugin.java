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
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.exception.OrganizationNotFoundException;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.dom.model.OrganizationUnit;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPermission;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-org-unit-plugin")
@MsgbusPlugin
public class OrganizationUnitPlugin {
	@Requires
	private OrganizationApi orgApi;

	@Requires
	private OrganizationUnitApi orgUnitApi;

	@MsgbusMethod
	public void getOrganizationUnits(Request req, Response resp) {
		Organization organization = orgApi.getOrganization(req.getOrgId());
		if (organization == null)
			throw new OrganizationNotFoundException(req.getOrgId());

		Collection<OrganizationUnit> orgUnits = orgUnitApi.getOrganizationUnits(organization);
		resp.put("org_units", Marshaler.marshal(orgUnits));
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom.org", code = "manage")
	public void createOrganizationUnit(Request req, Response resp) {
		OrganizationUnit orgUnit = toOrganizationUnit(req);
		orgUnitApi.createOrganizationUnit(orgUnit);
		resp.put("id", orgUnit.getId());
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom.org", code = "manage")
	public void updateOrganizationUnit(Request req, Response resp) {
		OrganizationUnit orgUnit = toOrganizationUnit(req);
		orgUnit.setId(req.getInteger("id"));
		orgUnitApi.updateOrganizationUnit(orgUnit);
	}

	private OrganizationUnit toOrganizationUnit(Request req) {
		OrganizationUnit orgUnit = new OrganizationUnit();
		orgUnit.setOrganization(orgApi.getOrganization(req.getOrgId()));
		if (req.getInteger("parent_id") != null)
			orgUnit.setParent(orgUnitApi.getOrganizationUnit(req.getInteger("parent_id")));
		else
			orgUnit.setParent(null);
		orgUnit.setName(req.getString("name"));
		orgUnit.setDomainController(req.getString("dc"));
		orgUnit.setFromLdap(false);
		return orgUnit;
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom.org", code = "manage")
	public void removeOrganizationUnit(Request req, Response resp) {
		orgUnitApi.removeOrganizationUnit(req.getInteger("id"));
	}
}
