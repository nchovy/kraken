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
package org.krakenapps.servlet.json;

import java.util.List;
import java.util.Map;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonHttpScript implements Script {
	final Logger logger = LoggerFactory.getLogger(JsonHttpScript.class.getName());

	private ScriptContext context;
	private JsonHttpServiceApi jsonManager;

	public JsonHttpScript(JsonHttpServiceApi jsonManager) {
		this.jsonManager = jsonManager;
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
			jsonManager.registerServlet(servletId, pathSpec);
			context.printf("json servlet registered: %s\n", servletId);
		} catch (Exception e) {
			context.println("failed to register servlet: " + e.toString());
			logger.error("failed to register servlet: ", e);
		}
	}

	/*
	 * Unregister JSON servlet from application context.
	 */
	public void unregister(String[] args) {
		String servletName = args[0];
		jsonManager.unregisterServlet(servletName);
	}

	public void filters(String[] args) {
		Map<String, List<String>> filters = jsonManager.getAvailableServices();
		context.println("===========================");
		context.println(" Available JSON Methods");
		context.println("===========================");

		for (String filterId : filters.keySet()) {
			context.println(filterId + ":");

			List<String> jsonMethods = filters.get(filterId);
			if (jsonMethods == null)
				continue;

			for (String jsonMethod : jsonMethods) {
				context.println("\t" + jsonMethod);
			}
		}
	}

}
