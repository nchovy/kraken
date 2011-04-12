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
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.exception.OrganizationNotFoundException;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPermission;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-org-plugin")
@MsgbusPlugin
public class OrganizationPlugin {
	@Requires
	private OrganizationApi organizationApi;

	@MsgbusMethod
	public void getOrganization(Request req, Response response) {
		int organizationId = req.getOrgId();
		Organization organization = organizationApi.getOrganization(organizationId);
		if (organization == null)
			throw new OrganizationNotFoundException(organizationId);

		response.put("result", organization.marshal());
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom.org", code = "manage")
	public void createOrganization(Request req, Response resp) {
		Organization organization = toOrganization(req);
		organizationApi.createOrganization(organization);
		resp.put("new_id", organization.getId());
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom.org", code = "manage")
	public void updateOrganization(Request req, Response resp) {
		Organization organization = toOrganization(req);
		organization.setId(req.getInteger("id"));
		organizationApi.updateOrganization(organization);
	}

	private Organization toOrganization(Request req) {
		Organization organization = new Organization();
		organization.setName(req.getString("name"));
		organization.setAddress(req.getString("address"));
		organization.setPhone(req.getString("phone"));
		organization.setDescription(req.getString("description"));
		organization.setDomainController(req.getString("dc"));
		organization.setBackupDomainController(req.getString("bdc"));
		return organization;
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom.org", code = "manage")
	public void removeOrganization(Request req, Response resp) {
		int id = req.getInteger("id");
		organizationApi.removeOrganization(id);
	}

}
