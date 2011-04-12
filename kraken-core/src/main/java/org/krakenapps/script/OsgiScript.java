/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Implements OSGi related script commands
 * 
 * @author xeraph
 * 
 */
public class OsgiScript implements Script {
	private ScriptContext context;
	private BundleContext bundleContext;

	public OsgiScript(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "Print details for specific OSGi service", arguments = { @ScriptArgument(name = "id", type = "long", description = "the service.id property of OSGi service") })
	public void properties(String[] args) {
		try {
			long id = Long.parseLong(args[0]);
			ServiceReference[] refs = bundleContext.getServiceReferences(null, "(service.id=" + id + ")");
			if (refs == null || refs.length == 0) {
				context.println("service " + id + " not found");
				return;
			}

			context.println("====== OSGi Service Properties ======");

			ServiceReference ref = refs[0];
			for (String key : ref.getPropertyKeys()) {
				context.printf("%s: %s\n", key, ref.getProperty(key));
			}

		} catch (InvalidSyntaxException e) {
			// ignore
		}
	}

	@ScriptUsage(description = "Search and ist all OSGi services", arguments = { @ScriptArgument(name = "query term", type = "string", description = "search for description", optional = true) })
	public void services(String[] args) {
		context.println("========= OSGi Services =========");
		List<ServiceInfo> list = new ArrayList<ServiceInfo>();
		try {
			ServiceReference[] refs = bundleContext.getServiceReferences(null, null);
			for (int i = 0; i < refs.length; i++) {
				Object o = bundleContext.getService(refs[i]);
				long id = (Long) refs[i].getProperty("service.id");
				String description = o.getClass().getName();
				if (refs[i].getProperty("service.pid") != null)
					description = refs[i].getProperty("service.pid").toString() + " (" + description + ")";

				if (args.length > 0 && description.indexOf(args[0]) < 0)
					continue;

				list.add(new ServiceInfo(id, description));
			}
		} catch (InvalidSyntaxException e) {
			// ignore
		}

		Collections.sort(list);

		for (ServiceInfo desc : list) {
			context.printf("[%3d] %s\n", desc.getId(), desc.getDescription());
		}
	}

	class ServiceInfo implements Comparable<ServiceInfo> {
		private long id;
		private String description;

		public ServiceInfo(long id, String description) {
			this.id = id;
			this.description = description;
		}

		public long getId() {
			return id;
		}

		public String getDescription() {
			return description;
		}

		@Override
		public int compareTo(ServiceInfo o) {
			return (int) (id - o.id);
		}
	}

}
