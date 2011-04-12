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
package org.krakenapps.ntp.script;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.ntp.NtpClient;

/**
 * @author delmitz
 */
public class NtpScript implements Script {
	private ScriptContext context;
	private NtpClient ntpClient;

	public NtpScript(NtpClient ntpClient) {
		this.ntpClient = ntpClient;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "get/set time server", arguments = { @ScriptArgument(name = "server address", type = "string", description = "new timeserver address", optional = true) })
	public void timeserver(String[] args) {
		if (args.length > 0) {
			try {
				InetAddress server = InetAddress.getByName(args[0]);
				ntpClient.setTimeServer(server);
			} catch (UnknownHostException e) {
				context.println("unknown host");
			}
		}

		context.println(ntpClient.getTimeServer());
	}

	@ScriptUsage(description = "get/set connection timeout", arguments = { @ScriptArgument(name = "timeout limit", type = "integer", description = "in millisecond", optional = true) })
	public void timeout(String[] args) {
		if (args.length > 0) {
			Integer millisecond = Integer.parseInt(args[0]);
			ntpClient.setTimeout(millisecond);
		}

		context.println(ntpClient.getTimeout());
	}

	public void sync(String[] args) {
		String str = ntpClient.sync();
		if (str != null)
			context.println(str);
		else
			context.println("synchronized failed.");
	}

}
