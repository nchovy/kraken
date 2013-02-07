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
package org.krakenapps.mail.impl;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.mail.MailerConfig;
import org.krakenapps.mail.MailerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailerScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(MailerScript.class);
	private ScriptContext context;
	private MailerRegistry registry;

	public MailerScript(MailerRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void list(String[] args) {
		context.println("Configurations");
		context.println("------------------");

		for (MailerConfig config : registry.getConfigs())
			context.println(config);
	}

	@ScriptUsage(description = "register smtp server configuration")
	public void register(String[] args) {
		MailerConfig config = new MailerConfig();
		try {
			context.print("Name? ");
			config.setName(context.readLine());

			context.print("SMTP Server? ");
			config.setHost(context.readLine());

			context.print("SMTP Port? ");
			config.setPort(Integer.parseInt(context.readLine()));
			if (config.getPort() < 1 || config.getPort() > 65535)
				throw new NumberFormatException();

			context.print("SMTP User? ");
			config.setUser(context.readLine());

			context.print("SMTP Password? ");
			config.setPassword(context.readPassword());

			config.setProtocol("smtp");
			config.setAuth(true);

			if (config.getPort() == 587 || config.getPort() == 465)
				config.setTls(true);

			registry.register(config);
			context.println("new configuration added");
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		} catch (NumberFormatException e) {
			context.println("invalid port number format");
		}
	}

	@ScriptUsage(description = "remove smtp configuration", arguments = { @ScriptArgument(name = "name", type = "string", description = "smtp configuration name") })
	public void unregister(String[] args) {
		try {
			registry.unregister(args[0]);
			context.println("smtp configuration removed");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken-mail: cannot remove smtp configuration", e);
		}
	}

	@ScriptUsage(description = "send mail", arguments = { @ScriptArgument(name = "name", type = "string", description = "smtp configuration name") })
	public void send(String[] args) throws NoSuchProviderException, MessagingException {
		try {
			context.print("From? ");
			String from = context.readLine();

			context.print("To? ");
			String to = context.readLine();

			context.print("Subject? ");
			String subject = context.readLine();

			context.println("Enter message, end with \".\" on a line by itself.");
			StringBuilder sb = new StringBuilder();

			while (true) {
				String line = context.readLine();
				if (line.equals("."))
					break;

				sb.append(line);
				sb.append("\n");
			}
			context.println("sending...");

			MailerConfig config = registry.getConfig(args[0]);
			registry.send(config, from, to, subject, sb.toString());
			context.println("complete");
		} catch (MessagingException e) {
			context.println("send failed. " + e);
			logger.error("kraken-mail: send failed", e);
		} catch (InterruptedException e) {
			context.println("interrupted");
		}
	}
}
