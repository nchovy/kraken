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

import java.util.Collection;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.mail.MailerConfig;
import org.krakenapps.mail.MailerRegistry;

@Component(name = "mailer-registry")
@Provides
public class MailerRegistryImpl implements MailerRegistry {
	@Requires
	private ConfigService conf;

	private ConfigCollection getCollection() {
		ConfigDatabase db = conf.ensureDatabase("kraken-mail");
		return db.ensureCollection(MailerConfig.class);
	}

	public Collection<MailerConfig> getConfigs() {
		ConfigCollection col = getCollection();
		return col.findAll().getDocuments(MailerConfig.class);
	}

	public MailerConfig getConfig(String name) {
		ConfigCollection col = getCollection();
		Config config = col.findOne(Predicates.field("name", name));
		if (config == null)
			return null;
		return config.getDocument(MailerConfig.class);
	}

	@Override
	public void register(MailerConfig config) {
		ConfigCollection col = getCollection();
		if (col.findOne(Predicates.field("name", config.getName())) != null)
			throw new IllegalArgumentException("already exist");
		col.add(PrimitiveConverter.serialize(config));
	}

	@Override
	public void unregister(String name) {
		ConfigCollection col = getCollection();
		Config c = col.findOne(Predicates.field("name", name));
		if (c == null)
			throw new IllegalArgumentException("not exist");
		col.remove(c);
	}

	@Override
	public Session getSession(MailerConfig config) {
		Authenticator auth = new SmtpAuthenticator(config.getUser(), config.getPassword());
		return Session.getInstance(config.getProperties(), auth);
	}

	private static class SmtpAuthenticator extends Authenticator {
		private PasswordAuthentication auth;

		public SmtpAuthenticator(String userName, String password) {
			auth = new PasswordAuthentication(userName, password);
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return auth;
		}
	}

	@Override
	public void send(MailerConfig config, String from, String to, String subject, String message) throws MessagingException {
		Session session = getSession(config);
		try {
			MimeMessage msg = new MimeMessage(session);

			msg.setFrom(new InternetAddress(from));
			msg.setRecipient(RecipientType.TO, new InternetAddress(to));
			msg.setSubject(subject);
			msg.setContent(message, "text/plain; charset=utf-8");

			Transport.send(msg);
		} finally {
			if (session != null)
				session.getTransport().close();
		}
	}
}
