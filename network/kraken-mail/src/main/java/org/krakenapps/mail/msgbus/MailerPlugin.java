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
package org.krakenapps.mail.msgbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.mail.MailerConfig;
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
	private final Logger logger = LoggerFactory.getLogger(MailerPlugin.class);

	@Requires
	private MailerRegistry registry;

	@MsgbusMethod
	public void getConfigs(Request req, Response resp) {
		List<Object> objs = new ArrayList<Object>();
		for (MailerConfig config : registry.getConfigs()) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("name", config.getName());
			m.put("host", config.getHost());
			m.put("port", config.getPort());
			m.put("user", config.getUser());
			objs.add(m);
		}

		resp.put("configs", objs);
	}

	@MsgbusMethod
	public void register(Request req, Response resp) {
		MailerConfig config = new MailerConfig();
		config.setName(req.getString("name"));
		config.setProtocol("smtp");
		config.setHost(req.getString("host"));
		config.setPort(req.getInteger("port"));
		config.setUser(req.getString("user"));
		config.setPassword(req.getString("password"));
		config.setAuth(true);

		if (config.getPort() < 1 || config.getPort() > 65535)
			throw new NumberFormatException("invalid port");

		if (config.getPort() == 587 || config.getPort() == 465)
			config.setTls(true);

		registry.register(config);
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void unregister(Request req, Response resp) {
		String name = req.getString("name");
		List<String> names = (List<String>) req.get("names");

		if (name != null)
			registry.unregister(name);

		if (names != null)
			for (String n : names)
				registry.unregister(n);
	}

	@MsgbusMethod
	public void send(Request req, Response resp) throws NoSuchProviderException, MessagingException {
		String confName = req.getString("config_name");
		String from = req.getString("from");
		String to = req.getString("to");
		String subject = req.getString("subject");
		String message = req.getString("message");

		MailerConfig config = registry.getConfig(confName);
		Session session = registry.getSession(config);
		MimeMessage msg = new MimeMessage(session);

		try {
			msg.setFrom(new InternetAddress(from));
			msg.setRecipient(RecipientType.TO, new InternetAddress(to));
			msg.setSubject(subject);
			msg.setContent(message, "text/plain; charset=utf-8");
			Transport.send(msg);
		} catch (MessagingException e) {
			logger.error("kraken-mail: send failed.", e);
		} finally {
			if (session != null)
				session.getTransport().close();
		}
	}
}
