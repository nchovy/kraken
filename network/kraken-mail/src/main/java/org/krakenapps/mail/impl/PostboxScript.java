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

import java.io.IOException;
import java.util.Arrays;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.mail.Postbox;
import org.krakenapps.mail.PostboxConfig;
import org.krakenapps.mail.PostboxRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostboxScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(PostboxScript.class);
	private ScriptContext context;
	private PostboxRegistry postbox;
	private static Postbox pb;

	public PostboxScript(PostboxRegistry postbox) {
		this.postbox = postbox;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void list(String[] args) {
		context.println("Configurations");
		context.println("------------------");

		for (PostboxConfig config : postbox.getConfigs())
			context.printf("name=%s, host=%s, port=%s, user=%s\n", config.getName(), config.getHost(), config.getPort(), config.getUser());
	}

	public void register(String[] args) {
		try {
			PostboxConfig config = new PostboxConfig();

			context.print("Name? ");
			config.setName(context.readLine());

			context.print("IMAP Server? ");
			config.setHost(context.readLine());

			context.print("IMAP Port? ");
			config.setPort(Integer.parseInt(context.readLine()));
			if (config.getPort() < 1 || config.getPort() > 65535)
				throw new NumberFormatException();

			context.print("IMAP User? ");
			config.setUser(context.readLine());

			context.print("IMAP Password? ");
			config.setPassword(context.readPassword());

			postbox.register(config);
			context.println("new configuration added");
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		} catch (NumberFormatException e) {
			context.println("invalid port number format");
		} catch (Exception e) {
			logger.error("kraken-mail: configuration failed", e);
		}
	}

	@ScriptUsage(description = "unregister config", arguments = { @ScriptArgument(name = "name", type = "string", description = "postbox config name") })
	public void unregister(String[] args) {
		try {
			postbox.unregister(args[0]);
			context.println("imap configuration removed");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken-mail: cannot remove imap configuration", e);
		}
	}

	@ScriptUsage(description = "open imap connection", arguments = { @ScriptArgument(name = "name", type = "string", description = "imap configuration name") })
	public void connect(String[] args) {
		if (pb != null && pb.isConnected()) {
			context.println("already connected.");
			return;
		}

		PostboxConfig config = postbox.getConfig(args[0]);
		pb = postbox.connect(config);
		if (pb == null)
			context.println("connect failed.");
		else
			context.println("connected");
	}

	@ScriptUsage(description = "listing messages", arguments = {
			@ScriptArgument(name = "folder", type = "string", description = "folder name"),
			@ScriptArgument(name = "page", type = "integer", description = "page number") })
	public void messages(String[] args) {
		if (pb == null || !pb.isConnected()) {
			context.println("not connected.");
			return;
		}

		try {
			pb.openFolder(args[0]);
			int page = Integer.parseInt(args[1]);
			for (Message msg : pb.getMessages((page - 1) * 10, page * 10)) {
				boolean isUnread = !msg.getFlags().contains(Flags.Flag.SEEN);
				context.println(String.format("%s[%d] %s", isUnread ? "*" : " ", msg.getMessageNumber(), msg.getSubject()));
			}
		} catch (MessagingException e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "listing messages", arguments = {
			@ScriptArgument(name = "folder", type = "string", description = "folder name"),
			@ScriptArgument(name = "mail", type = "integer", description = "mail number") })
	public void message(String[] args) {
		if (pb == null || !pb.isConnected()) {
			context.println("not connected.");
			return;
		}

		try {
			pb.openFolder(args[0]);
			Message msg = pb.getMessage(Integer.parseInt(args[1]));

			context.println("from: " + Arrays.toString(msg.getFrom()));
			context.println("reply to: " + Arrays.toString(msg.getReplyTo()));
			context.println("received: " + msg.getReceivedDate());
			context.println("subject: " + msg.getSubject());
			context.println("");
			try {
				if (msg.getContent() instanceof MimeMultipart) {
					MimeMultipart content = (MimeMultipart) msg.getContent();
					BodyPart part = content.getBodyPart(0);
					context.println(toSimpleString(part.getContent().toString()));
				} else
					context.println(toSimpleString(msg.getContent().toString()));
			} catch (IOException e) {
				context.println(e.getMessage());
			}
		} catch (MessagingException e) {
			context.println(e.getMessage());
		}
	}

	private String toSimpleString(String str) {
		return str.replaceAll("&nbsp;", " ").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("<[^>]*>", "")
				.replaceAll("(\t|\\t| )+", " ");
	}

	public void disconnect(String[] args) {
		if (pb == null || !pb.isConnected()) {
			context.println("already disconnected.");
			return;
		}

		try {
			pb.disconnect();
			context.println("disconnected");
		} catch (MessagingException e) {
			context.println("disconnect failed: " + e.getMessage());
			logger.error("kraken-mail: cannot close imap", e);
		}
	}
}
