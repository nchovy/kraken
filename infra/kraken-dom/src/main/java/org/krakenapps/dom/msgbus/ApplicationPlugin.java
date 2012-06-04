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
import org.krakenapps.dom.api.ApplicationApi;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.model.Application;
import org.krakenapps.dom.model.ApplicationGroup;
import org.krakenapps.dom.model.Vendor;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-app-plugin")
@MsgbusPlugin
public class ApplicationPlugin {
	@Requires
	private ConfigManager conf;

	@Requires
	private ApplicationApi appApi;

	@MsgbusMethod
	public void getVendors(Request req, Response resp) {
		Collection<Vendor> vendors = appApi.getVendors(req.getOrgDomain());
		resp.put("vendors", PrimitiveConverter.serialize(vendors));
	}

	@MsgbusMethod
	public void getVendor(Request req, Response resp) {
		String guid = req.getString("guid");
		Vendor vendor = appApi.getVendor(req.getOrgDomain(), guid);
		resp.put("vendor", PrimitiveConverter.serialize(vendor));
	}

	@MsgbusMethod
	public void createVendor(Request req, Response resp) {
		Vendor vendor = (Vendor) PrimitiveConverter.overwrite(new Vendor(), req.getParams(),
				conf.getParseCallback(req.getOrgDomain()));
		appApi.createVendor(req.getOrgDomain(), vendor);
		resp.put("guid", vendor.getGuid());
	}

	@MsgbusMethod
	public void updateVendor(Request req, Response resp) {
		Vendor before = appApi.getVendor(req.getOrgDomain(), req.getString("guid"));
		Vendor vendor = (Vendor) PrimitiveConverter.overwrite(before, req.getParams(), conf.getParseCallback(req.getOrgDomain()));
		appApi.updateVendor(req.getOrgDomain(), vendor);
	}

	@MsgbusMethod
	public void removeVendor(Request req, Response resp) {
		String guid = req.getString("guid");
		appApi.removeVendor(req.getOrgDomain(), guid);
	}

	@MsgbusMethod
	public void getApplications(Request req, Response resp) {
		Collection<Application> applications = appApi.getApplications(req.getOrgDomain());
		resp.put("apps", PrimitiveConverter.serialize(applications));
	}

	@MsgbusMethod
	public void getApplication(Request req, Response resp) {
		String guid = req.getString("guid");
		Application application = appApi.getApplication(req.getOrgDomain(), guid);
		resp.put("app", PrimitiveConverter.serialize(application));
	}

	@MsgbusMethod
	public void createApplication(Request req, Response resp) {
		Application application = (Application) PrimitiveConverter.overwrite(new Application(), req.getParams(),
				conf.getParseCallback(req.getOrgDomain()));
		appApi.createApplication(req.getOrgDomain(), application);
		resp.put("guid", application.getGuid());
	}

	@MsgbusMethod
	public void updateApplication(Request req, Response resp) {
		Application before = appApi.getApplication(req.getOrgDomain(), req.getString("guid"));
		Application application = (Application) PrimitiveConverter.overwrite(before, req.getParams(),
				conf.getParseCallback(req.getOrgDomain()));
		appApi.updateApplication(req.getOrgDomain(), application);
	}

	@MsgbusMethod
	public void removeApplication(Request req, Response resp) {
		String guid = req.getString("guid");
		appApi.removeApplication(req.getOrgDomain(), guid);
	}

	@MsgbusMethod
	public void getApplicationGroups(Request req, Response resp) {
		Collection<ApplicationGroup> groups = appApi.getApplicationGroups(req.getOrgDomain());
		resp.put("groups", PrimitiveConverter.serialize(groups));
	}

	@MsgbusMethod
	public void getApplicationGroup(Request req, Response resp) {
		String guid = req.getString("guid");
		ApplicationGroup group = appApi.getApplicationGroup(req.getOrgDomain(), guid);
		resp.put("group", PrimitiveConverter.serialize(group));
	}

	@MsgbusMethod
	public void createApplicationGroup(Request req, Response resp) {
		ApplicationGroup group = (ApplicationGroup) PrimitiveConverter.overwrite(new ApplicationGroup(), req.getParams(),
				conf.getParseCallback(req.getOrgDomain()));
		appApi.createApplicationGroup(req.getOrgDomain(), group);
		resp.put("guid", group.getGuid());
	}

	@MsgbusMethod
	public void updateApplicationGroup(Request req, Response resp) {
		ApplicationGroup before = appApi.getApplicationGroup(req.getOrgDomain(), req.getString("guid"));
		ApplicationGroup group = (ApplicationGroup) PrimitiveConverter.overwrite(before, req.getParams(),
				conf.getParseCallback(req.getOrgDomain()));
		appApi.updateApplicationGroup(req.getOrgDomain(), group);
	}

	@MsgbusMethod
	public void removeApplicationGroup(Request req, Response resp) {
		String guid = req.getString("guid");
		appApi.removeApplicationGroup(req.getOrgDomain(), guid);
	}

	@MsgbusMethod
	public void removeApplications(Request req, Response resp) {
		@SuppressWarnings("unchecked")
		Collection<String> guids = (Collection<String>) req.get("guids");
		appApi.removeApplications(req.getOrgDomain(), guids);

	}
}
