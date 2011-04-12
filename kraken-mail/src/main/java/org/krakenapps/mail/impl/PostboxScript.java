package org.krakenapps.mail.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.mail.PostboxApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostboxScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(PostboxScript.class.getName());
	private ScriptContext context;
	private PostboxApi postbox;
	private static Store store;

	public PostboxScript(PostboxApi postbox) {
		this.postbox = postbox;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void list(String[] args) {
		context.println("Configurations");
		context.println("------------------");

		Map<String, Properties> m = postbox.getConfigs();
		for (String name : m.keySet()) {
			Properties props = m.get(name);

			Object host = props.get("mail.imap.host");
			Object port = props.get("mail.imap.port");
			Object user = props.get("mail.imap.user");

			context.printf("name=%s, host=%s, port=%s, user=%s", name, host, port, user);
			context.println("");
		}
	}

	public void register(String[] args) {
		try {
			Properties props = new Properties();
			context.print("Name? ");
			String name = context.readLine();

			context.print("IMAP Server? ");
			String host = context.readLine();

			context.print("IMAP Port? ");
			int port = Integer.parseInt(context.readLine());
			if (port < 1 || port > 65535)
				throw new NumberFormatException();

			context.print("IMAP User? ");
			String user = context.readLine();

			context.print("IMAP Password? ");
			String password = context.readPassword();

			props.setProperty("mail.imap.host", host);
			props.setProperty("mail.imap.port", Integer.toString(port));
			props.setProperty("mail.imap.user", user);
			props.setProperty("mail.imap.password", password);

			postbox.register(name, props);
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
		if (store != null && store.isConnected()) {
			context.println("already connected.");
			return;
		}
		store = postbox.connect(args[0]);
		if (store == null)
			context.println("connect failed.");
		context.println("connect");
	}

	@ScriptUsage(description = "listing messages", arguments = {
			@ScriptArgument(name = "folder", type = "string", description = "folder name"),
			@ScriptArgument(name = "page", type = "integer", description = "page number") })
	public void messages(String[] args) {
		if (store == null || !store.isConnected()) {
			context.println("not connected.");
			return;
		}

		try {
			Folder folder = store.getFolder(args[0]);
			folder.open(Folder.READ_ONLY);
			List<Message> msgs = Arrays.asList(folder.getMessages());
			Collections.reverse(msgs);

			int page = Integer.parseInt(args[1]);
			if (page < 1)
				return;

			int from = 10 * (page - 1);
			int to = Math.min(from + 10, msgs.size());
			for (int i = from; i < to; i++) {
				Message msg = msgs.get(i);
				boolean isUnread = !msg.getFlags().contains(Flags.Flag.SEEN);
				context.println(String.format("%s[%d] %s", isUnread ? "*" : " ", msg.getMessageNumber(),
						msg.getSubject()));
			}

			folder.close(false);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	@ScriptUsage(description = "listing messages", arguments = {
			@ScriptArgument(name = "folder", type = "string", description = "folder name"),
			@ScriptArgument(name = "mail", type = "integer", description = "mail number") })
	public void message(String[] args) {
		if (store == null || !store.isConnected()) {
			context.println("not connected.");
			return;
		}

		try {
			Folder folder = store.getFolder(args[0]);
			folder.open(Folder.READ_ONLY);
			int msgnum = Integer.parseInt(args[1]);
			if (msgnum < 1 || msgnum > folder.getMessageCount())
				return;
			Message msg = folder.getMessage(msgnum);

			context.println("from: " + addressToString(msg.getFrom()));
			context.println("reply to: " + addressToString(msg.getReplyTo()));
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
				e.printStackTrace();
			}

			folder.close(false);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	private String addressToString(Address[] addrs) {
		String str = null;
		for (Address addr : addrs) {
			if (str == null)
				str = addr.toString();
			else
				str += ", " + addr.toString();
		}
		return str;
	}

	private String toSimpleString(String str) {
		return str.replaceAll("&nbsp;", " ").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("<[^>]*>", "")
				.replaceAll("(\t|\\t| )+", " ");
	}

	public void disconnect(String[] args) {
		if (store == null || !store.isConnected()) {
			context.println("already disconnected.");
			return;
		}
		try {
			store.close();
		} catch (MessagingException e) {
			context.println("disconnect failed: " + e.getMessage());
			logger.error("kraken-mail: cannot close imap", e);
		}
		context.println("disconnect");
	}
}
