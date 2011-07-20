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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.ApplicationApi;
import org.krakenapps.dom.model.Application;
import org.krakenapps.dom.model.ApplicationMetadata;
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
		resp.put("guid", vendor.getGuid());
	}

	@MsgbusMethod
	public void updateVendor(Request req, Response resp) {
		String guid = req.getString("guid");
		String name = req.getString("name");
		appApi.updateVendor(guid, name);
	}

	@MsgbusMethod
	public void removeVendor(Request req, Response resp) {
		String guid = req.getString("guid");
		appApi.removeVendor(guid);
	}

	@MsgbusMethod
	public void getApplications(Request req, Response resp) {
		String vendorGuid = req.getString("vendor_guid");
		Collection<Application> apps = appApi.getApplications(vendorGuid);
		resp.put("apps", Marshaler.marshal(apps));
	}

	@MsgbusMethod
	public void getApplication(Request req, Response resp) {
		String guid = req.getString("guid");
		Application app = appApi.getApplication(guid);
		if (app == null) {
			resp.put("app", null);
			return;
		}

		Map<String, Object> m = app.marshal();
		m.put("metadata", marshal(app.getMetadatas()));

		resp.put("app", m);
	}

	private Map<String, String> marshal(List<ApplicationMetadata> l) {
		Map<String, String> m = new HashMap<String, String>();
		for (ApplicationMetadata meta : l)
			m.put(meta.getKey().getName(), meta.getValue());

		return m;
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void createApplication(Request req, Response resp) {
		String vendorGuid = req.getString("vendor_guid");
		String name = req.getString("name");
		String platform = req.getString("platform");
		Map<String, String> props = (Map<String, String>) req.get("metadata");

		Application app = appApi.createApplication(vendorGuid, name, platform, props);
		resp.put("guid", app.getGuid());
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void updateApplication(Request req, Response resp) {
		String guid = req.getString("guid");
		String name = req.getString("name");
		Map<String, String> props = (Map<String, String>) req.get("metadata");

		appApi.updateApplication(guid, name, props);
	}

	@MsgbusMethod
	public void removeApplication(Request req, Response resp) {
		String guid = req.getString("guid");
		appApi.removeApplication(guid);
	}
}
