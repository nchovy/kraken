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
package org.krakenapps.servlet.text;

import java.util.Map;
import java.util.Set;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;

public class TextHttpScript implements Script {
	private ScriptContext context;
	private TextHttpServiceApi manager;

	public TextHttpScript(TextHttpServiceApi manager) {
		this.manager = manager;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void register(String[] args) {
		String servletId = args[0];
		String pathSpec = args[1];

		try {
			manager.registerServlet(servletId, pathSpec);
			context.printf("text servlet registered: %s\n", servletId);
		} catch (Exception e) {
			context.println("failed to register servlet: " + e.toString());
		}
	}

	public void filters(String[] args) {
		Map<String, Set<String>> mappings = manager.getAvailableFilters();
		context.println("Available Text Methods");
		context.println("===========================");

		for (String serviceName : mappings.keySet()) {
			context.println(serviceName + ":");

			Set<String> textMethods = mappings.get(serviceName);
			if (textMethods == null)
				continue;

			for (String textMethod : textMethods) {
				context.println("\t" + textMethod);
			}
		}
	}
}
