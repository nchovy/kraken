/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.lookup.mac.impl;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.lookup.mac.MacLookupService;
import org.krakenapps.lookup.mac.Vendor;

public class MacLookupScript implements Script {
	private ScriptContext context;
	private MacLookupService macLookup;

	public MacLookupScript(MacLookupService macLookup) {
		this.macLookup = macLookup;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "search vendor by mac oui", arguments = { 
			@ScriptArgument(name = "mac", type = "string", description = "mac address") })
	public void search(String[] args) {
		String oui = extractOui(args[0]);
		Vendor vendor = macLookup.find(oui);

		context.println(vendor.toString());
	}

	private String extractOui(String macAddress) {
		macAddress = macAddress.trim().replace("-", "").replace(":", "");
		return macAddress.substring(0, 6);
	}
}
