/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.webconsole.plugins;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.BundleManager;
import org.krakenapps.api.MavenResolveException;
import org.krakenapps.api.ProgressMonitor;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

@Component(name = "webconsole-bundle-plugin")
@MsgbusPlugin
public class BundlePlugin {
	private BundleContext bc;

	@Requires
	private BundleManager bundleManager;

	public BundlePlugin(BundleContext bc) {
		this.bc = bc;
	}

	@MsgbusMethod
	public void getBundles(Request req, Response resp) {
		List<Object> l = new ArrayList<Object>();
		for (Bundle bundle : bc.getBundles()) {
			l.add(marshal(bundle));
		}

		resp.put("bundles", l);
	}

	@MsgbusMethod
	public void startBundle(Request req, Response resp) {
		int bundleId = req.getInteger("bundle_id");
		Bundle bundle = bc.getBundle(bundleId);
		if (bundle == null)
			throw new IllegalArgumentException("bundle not found: " + bundleId);

		try {
			bundle.start();
		} catch (BundleException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	@MsgbusMethod
	public void stopBundle(Request req, Response resp) {
		int bundleId = req.getInteger("bundle_id");
		Bundle bundle = bc.getBundle(bundleId);
		if (bundle == null)
			throw new IllegalArgumentException("bundle not found: " + bundleId);

		try {
			bundle.stop();
		} catch (BundleException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	@MsgbusMethod
	public void installBundle(Request req, Response resp) {
		if (req.has("path")) {
			String path = req.getString("path");
			long bundleId = bundleManager.installBundle(path);
			resp.put("bundle_id", bundleId);
		} else if (req.has("group_id") && req.has("artifact_id") && req.has("version")) {
			String groupId = req.getString("group_id");
			String artifactId = req.getString("artifact_id");
			String version = req.getString("version");

			try {
				bundleManager.installBundle(new NullMonitor(), groupId, artifactId, version);
			} catch (MavenResolveException e) {
				throw new IllegalStateException(e);
			}
		} else {
			throw new IllegalStateException("invalid install parameter");
		}
	}

	@MsgbusMethod
	public void uninstallBundle(Request req, Response resp) {
		int bundleId = req.getInteger("bundle_id");
		bundleManager.uninstallBundle(bundleId);
	}

	@MsgbusMethod
	public void updateBundle(Request req, Response resp) {
		int bundleId = req.getInteger("bundle_id");
		bundleManager.updateBundle(bundleId);
	}

	@MsgbusMethod
	public void refresh(Request req, Response resp) {
		bundleManager.refresh();
	}

	private Map<String, Object> marshal(Bundle b) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("id", b.getBundleId());
		m.put("name", b.getSymbolicName());
		m.put("version", b.getHeaders().get("Bundle-Version"));
		m.put("vendor", b.getHeaders().get("Bundle-Vendor"));
		m.put("last_modified", dateFormat.format(new Date(b.getLastModified())));
		m.put("location", b.getLocation());
		m.put("export_package", b.getHeaders().get("Export-Package"));
		m.put("import_package", b.getHeaders().get("Import-Package"));
		m.put("built_by", b.getHeaders().get("Built-By"));
		m.put("license", b.getHeaders().get("Bundle-License"));
		m.put("url", b.getHeaders().get("Bundle-DocURL"));
		m.put("status", getBundleState(b.getState()));
		return m;
	}

	private String getBundleState(int s) {
		switch (s) {
		case Bundle.ACTIVE:
			return "ACTIVE";
		case Bundle.INSTALLED:
			return "INSTALLED";
		case Bundle.RESOLVED:
			return "RESOLVED";
		case Bundle.STARTING:
			return "STARTING";
		case Bundle.STOPPING:
			return "STOPPING";
		case Bundle.UNINSTALLED:
			return "UNINSTALLED";
		default:
			return "UNKNOWN";
		}
	}

	private static class NullMonitor implements ProgressMonitor {
		@Override
		public void writeln(String arg0) {
		}

		@Override
		public void write(String arg0) {
		}
	}
}
