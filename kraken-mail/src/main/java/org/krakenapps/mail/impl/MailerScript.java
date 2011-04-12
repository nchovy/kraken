package org.krakenapps.mail.impl;

import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.mail.MailerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailerScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(MailerScript.class.getName());
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

		Map<String, Properties> m = registry.getConfigs();
		for (String name : m.keySet()) {
			Properties props = m.get(name);

			Object host = props.get("mail.smtp.host");
			Object port = props.get("mail.smtp.port");
			Object user = props.get("mail.smtp.user");

			context.printf("name=%s, host=%s, port=%s, user=%s", name, host, port, user);
			context.println("");
		}
	}

	@ScriptUsage(description = "register smtp server configuration")
	public void register(String[] args) {
		try {
			Properties props = new Properties();
			context.print("Name? ");
			String name = context.readLine();

			context.print("SMTP Server? ");
			String smtpHost = context.readLine();

			context.print("SMTP Port? ");
			int smtpPort = Integer.parseInt(context.readLine());
			if (smtpPort < 1 || smtpPort > 65535)
				throw new NumberFormatException();

			context.print("SMTP User? ");
			String user = context.readLine();

			context.print("SMTP Password? ");
			String password = context.readPassword();

			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.host", smtpHost);
			props.put("mail.smtp.port", Integer.toString(smtpPort));
			props.put("mail.smtp.user", user);
			props.put("mail.smtp.password", password);
			props.put("mail.smtp.auth", "true");

			if (smtpPort == 587 || smtpPort == 465) {
				props.put("mail.smtp.starttls.enable", "true");
			}

			registry.register(name, props);
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
	public void send(String[] args) {
		String name = args[0];
		Session session = registry.getSession(name);

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

			MimeMessage msg = new MimeMessage(session);

			InternetAddress fromAddr = new InternetAddress(from);
			InternetAddress toAddr = new InternetAddress(to);

			msg.setFrom(fromAddr);
			msg.setRecipient(RecipientType.TO, toAddr);
			msg.setSubject(subject);
			msg.setContent(sb.toString(), "text/plain; charset=utf-8");

			context.println("sending...");
			Transport.send(msg);
			context.println("completed");
		} catch (MessagingException e) {
			context.println("send failed. " + e.getMessage());
			logger.error("kraken-mail: send failed", e);
		} catch (InterruptedException e) {
			context.println("interrupted");
		}

	}
}
