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
package org.krakenapps.servlet.xml;

import java.util.Map;
import java.util.Set;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;

public class XmlHttpScript implements Script {
	private ScriptContext context;
	private XmlHttpServiceApi xmlManager;

	public XmlHttpScript(XmlHttpServiceApi xmlManager) {
		this.xmlManager = xmlManager;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	/*
	 * Register JSON servlet to application context.
	 */
	public void register(String[] args) {
		String servletId = args[0];
		String pathSpec = args[1];

		try {
			xmlManager.registerServlet(servletId, pathSpec);
			context.printf("xml http servlet registered: %s\n", servletId);
		} catch (Exception e) {
			context.println("failed to register servlet: " + e.toString());
		}
	}

	public void filters(String[] args) {
		Map<String, Set<String>> mappings = xmlManager.getXmlHttpMappings();
		context.println("Available XML Methods");
		context.println("===========================");

		for (String serviceName : mappings.keySet()) {
			context.println(serviceName + ":");

			Set<String> xmlMethods = mappings.get(serviceName);
			if (xmlMethods == null)
				continue;

			for (String xmlMethod : xmlMethods) {
				context.println("\t" + xmlMethod);
			}
		}
	}
}
