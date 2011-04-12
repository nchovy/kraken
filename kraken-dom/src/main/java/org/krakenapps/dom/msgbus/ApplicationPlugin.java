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
import org.krakenapps.dom.api.ApplicationApi;
import org.krakenapps.dom.model.Application;
import org.krakenapps.dom.model.ApplicationVersion;
import org.krakenapps.dom.model.Vendor;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-app-plugin")
@MsgbusPlugin
public class ApplicationPlugin {
	@Requires
	private ApplicationApi appApi;

	@MsgbusMethod
	public void getVendors(Request req, Response resp) {
		Collection<Vendor> vendors = appApi.getVendors();
		resp.put("vendors", Marshaler.marshal(vendors));
	}

	@MsgbusMethod
	public void createVendor(Request req, Response resp) {
		String name = req.getString("name");
		Vendor vendor = appApi.createVendor(name);
		resp.put("vendor_id", vendor.getId());
	}

	@MsgbusMethod
	public void updateVendor(Request req, Response resp) {
		int id = req.getInteger("id");
		String name = req.getString("name");
		appApi.updateVendor(id, name);
	}

	@MsgbusMethod
	public void removeVendor(Request req, Response resp) {
		int id = req.getInteger("id");
		appApi.removeVendor(id);
	}

	@MsgbusMethod
	public void getApplications(Request req, Response resp) {
		String vendorName = req.getString("vendor_name");
		resp.put("apps", Marshaler.marshal(appApi.getApplications(vendorName)));
	}

	@MsgbusMethod
	public void createApplication(Request req, Response resp) {
		String vendorName = req.getString("vendor_name");
		String name = req.getString("app_name");

		Application app = appApi.createApplication(vendorName, name);
		resp.put("app_id", app.getId());
	}

	@MsgbusMethod
	public void updateApplication(Request req, Response resp) {
		int id = req.getInteger("app_id");
		String name = req.getString("app_name");

		appApi.updateApplication(id, name);
	}

	@MsgbusMethod
	public void removeApplication(Request req, Response resp) {
		int id = req.getInteger("app_id");
		appApi.removeApplication(id);
	}
	
	@MsgbusMethod
	public void getApplicationVersions(Request req, Response resp) {
		String vendorName = req.getString("vendor_name");
		String appName = req.getString("app_name");
		
		Collection<ApplicationVersion> versions = appApi.getApplicationVersions(vendorName, appName);
		resp.put("versions", Marshaler.marshal(versions));
	}

	@MsgbusMethod
	public void createApplicationVersion(Request req, Response resp) {
		String vendorName = req.getString("vendor_name");
		String appName = req.getString("app_name");
		String version = req.getString("version");

		ApplicationVersion appVersion = appApi.createApplicationVersion(vendorName, appName, version);
		resp.put("version_id", appVersion.getId());
	}

	@MsgbusMethod
	public void updateApplicationVersion(Request req, Response resp) {
		int id = req.getInteger("version_id");
		String version = req.getString("version");

		appApi.updateApplicationVersion(id, version);
	}

	@MsgbusMethod
	public void removeApplicationVersion(Request req, Response resp) {
		int id = req.getInteger("version_id");
		appApi.removeApplicationVersion(id);
	}
}
