package org.krakenapps.mail.msgbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.mail.MailerRegistry;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "mailer-plugin")
@MsgbusPlugin
public class MailerPlugin {
	private final Logger logger = LoggerFactory.getLogger(MailerPlugin.class.getName());

	@Requires
	private MailerRegistry registry;

	@MsgbusMethod
	public void getConfigs(Request req, Response resp) {
		List<Object> objs = new ArrayList<Object>();
		Map<String, Properties> configs = registry.getConfigs();
		for (String name : configs.keySet()) {
			Map<String, Object> m = new HashMap<String, Object>();
			Properties props = configs.get(name);
			m.put("name", name);
			m.put("host", props.get("mail.smtp.host"));
			m.put("port", props.get("mail.smtp.port"));
			m.put("user", props.get("mail.smtp.user"));
			objs.add(m);
		}

		resp.put("configs", objs);
	}

	@MsgbusMethod
	public void register(Request req, Response resp) {
		String name = req.getString("name");
		String host = req.getString("host");
		Integer port = req.getInteger("port");
		String user = req.getString("user");
		String password = req.getString("password");

		if (port < 1 || port > 65535)
			throw new NumberFormatException("invalid port");

		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", String.valueOf(port));
		props.put("mail.smtp.user", user);
		props.put("mail.smtp.password", password);
		props.put("mail.smtp.auth", "true");
		if (port == 587 || port == 465)
			props.put("mail.smtp.starttls.enable", "true");

		registry.register(name, props);
	}

	@MsgbusMethod
	public void unregister(Request req, Response resp) {
		String name = req.getString("name");
		registry.unregister(name);
	}

	@MsgbusMethod
	public void send(Request req, Response resp) {
		String confName = req.getString("config_name");
		String from = req.getString("from");
		String to = req.getString("to");
		String subject = req.getString("subject");
		String message = req.getString("message");

		Session session = registry.getSession(confName);
		MimeMessage msg = new MimeMessage(session);

		try {
			msg.setFrom(new InternetAddress(from));
			msg.setRecipient(RecipientType.TO, new InternetAddress(to));
			msg.setSubject(subject);
			msg.setContent(message, "text/plain; charset=utf-8");
			Transport.send(msg);
		} catch (MessagingException e) {
			logger.error("kraken-mail: send failed. " + e.getMessage());
		}
	}
}
