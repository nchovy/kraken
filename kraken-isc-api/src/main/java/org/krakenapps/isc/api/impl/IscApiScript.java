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
package org.krakenapps.isc.api.impl;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.isc.api.IscClient;
import org.krakenapps.isc.api.IscClientConfig;
import org.krakenapps.xmlrpc.XmlRpcFaultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IscApiScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(IscApiScript.class.getName());

	private IscClient client;
	private IscClientConfig config;
	private ScriptContext context;

	public IscApiScript(IscClient client, IscClientConfig config) {
		this.client = client;
		this.config = config;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void threatcon(String[] args) {
		try {
			if (client == null) {
				context.println("set api key first");
				return;
			}

			context.println(client.call("threatcon.get"));
		} catch (XmlRpcFaultException e) {
			context.println("failed: " + e.getFaultString());
		} catch (Exception e) {
			logger.error("kraken isc api: cannot get threatcon", e);
		}
	}

	public void apikey(String[] args) {
		String apiKey = config.getApiKey();
		context.println(apiKey == null ? "not set" : apiKey);
	}

	@ScriptUsage(description = "set or unset api key", arguments = { @ScriptArgument(type = "string", name = "apikey", description = "new apikey", optional = true) })
	public void setkey(String[] args) {
		if (args.length > 0) {
			config.setApiKey(args[0]);
			context.println("set");
		} else {
			config.setApiKey(null);
			context.println("unset");
		}
	}
}
