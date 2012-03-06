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
package org.krakenapps.servlet.xmlrpc.internal;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.servlet.xmlrpc.XmlRpcMethodRegistry;

public class XmlRpcServletScript implements Script {
	private ScriptContext context;
	private XmlRpcMethodRegistry registry;

	public XmlRpcServletScript(XmlRpcMethodRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void list(String[] args) {
		String filter = null;
		if (args.length > 0)
			filter = args[0];

		context.println("XMLRPC Methods");
		context.println("----------------");

		for (String method : registry.getMethods()) {
			if (filter != null && !method.contains(filter))
				continue;

			context.println(method);
		}
	}

}
