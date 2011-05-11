package org.krakenapps.radius.script;

import java.net.InetAddress;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.radius.client.RadiusClient;
import org.krakenapps.radius.client.auth.PapAuthenticator;
import org.krakenapps.radius.protocol.RadiusPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadiusScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(RadiusScript.class.getName());

	private ScriptContext context;

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "authenticate using pap method", arguments = {
			@ScriptArgument(name = "server ip", type = "string", description = "radius server ip address"),
			@ScriptArgument(name = "shared secret", type = "string", description = "shared secret"),
			@ScriptArgument(name = "username", type = "string", description = "user name"),
			@ScriptArgument(name = "password", type = "string", description = "password") })
	public void papauth(String[] args) {
		try {
			InetAddress addr = InetAddress.getByName(args[0]);
			String sharedSecret = args[1];
			String userName = args[2];
			String password = args[3];

			RadiusClient client = new RadiusClient(addr, sharedSecret);
			RadiusPacket response = client.authenticate(new PapAuthenticator(userName, password));
			context.println(response);
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken radius: pap authentication failed", e);
		}
	}
}
