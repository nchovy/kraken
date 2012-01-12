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
package org.krakenapps.slpolicy;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;

public class SilverlightPolicyScript implements Script {
	private SilverlightPolicyServer server;
	private ScriptContext context;

	public SilverlightPolicyScript(SilverlightPolicyServer server) {
		this.server = server;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void open(String[] args) {
		try {
			server.open();
			context.println("silverlight policy server opened");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	public void close(String[] args) {
		try {
			server.close();
			context.println("silverlight policy server closed");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}
}
