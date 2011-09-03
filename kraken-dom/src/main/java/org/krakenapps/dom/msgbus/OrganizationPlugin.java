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
import org.krakenapps.dom.api.OrganizationParameterApi;
import org.krakenapps.dom.exception.OrganizationNotFoundException;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.dom.model.OrganizationParameter;
import org.krakenapps.msgbus.Marshaler;
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

	@Requires
	private OrganizationParameterApi organizationParameterApi;

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

	@MsgbusMethod
	@MsgbusPermission(group = "dom.org", code = "manage")
	public void getOrganizationParameters(Request req, Response resp) {
		Collection<OrganizationParameter> parameters = organizationParameterApi.getOrganizationParameters(req
				.getOrgId());
		resp.put("result", Marshaler.marshal(parameters));
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom.org", code = "manage")
	public void getOrganizationParameter(Request req, Response resp) {
		OrganizationParameter parameter = null;
		if (req.has("id"))
			parameter = organizationParameterApi.getOrganizationParameter(req.getOrgId(), req.getInteger("id"));
		else if (req.has("name"))
			parameter = organizationParameterApi.getOrganizationParameter(req.getOrgId(), req.getString("name"));
		resp.put("result", parameter.marshal());
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom.org", code = "manage")
	public void createOrganizationParameter(Request req, Response resp) {
		OrganizationParameter parameter = toOrganizationParameter(req);
		organizationParameterApi.createOrganizationParameter(req.getOrgId(), parameter);
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom.org", code = "manage")
	public void updateOrganizationParameter(Request req, Response resp) {
		OrganizationParameter parameter = toOrganizationParameter(req);
		organizationParameterApi.updateOrganizationParameter(req.getOrgId(), parameter);
	}

	private OrganizationParameter toOrganizationParameter(Request req) {
		OrganizationParameter parameter = new OrganizationParameter();
		parameter.setId(req.getInteger("id"));
		parameter.setOrganization(organizationApi.getOrganization(req.getInteger("org_id")));
		parameter.setName(req.getString("name"));
		parameter.setValue(req.getString("value"));
		return parameter;
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom.org", code = "manage")
	public void removeOrganizationParameter(Request req, Response resp) {
		int id = req.getInteger("id");
		organizationParameterApi.removeOrganizationParameter(req.getOrgId(), id);
	}
}
