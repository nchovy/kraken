package org.krakenapps.mail.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
import org.krakenapps.mail.MailerRegistry;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "mailer-registry")
@Provides
public class MailerRegistryImpl implements MailerRegistry {
	private final Logger logger = LoggerFactory.getLogger(MailerRegistryImpl.class.getName());

	@Requires
	private PreferencesService prefsvc;

	public Map<String, Properties> getConfigs() {
		Map<String, Properties> m = new HashMap<String, Properties>();

		try {
			Preferences root = getPreferences();
			for (String name : root.childrenNames()) {
				m.put(name, getConfig(name));
			}
		} catch (BackingStoreException e) {
			throw new IllegalStateException(e.getMessage());
		}

		return m;
	}

	public Properties getConfig(String name) {
		try {
			Preferences root = getPreferences();
			if (!root.nodeExists(name)) {
				throw new IllegalStateException("smtp server not found: " + name);
			}

			Properties props = new Properties();

			Preferences p = root.node(name);
			for (String key : p.keys()) {
				String value = p.get(key, null);
				props.put(key, value);
			}
			
			props.put("mail.smtp.connectiontimeout", 5000);
			props.put("mail.smtp.timeout", 5000);
			return props;
		} catch (BackingStoreException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	@Override
	public Session getSession(String name) {
		Properties props = getConfig(name);
		String user = props.getProperty("mail.smtp.user");
		String password = props.getProperty("mail.smtp.password");
		
		for (Object s : props.keySet()) {
			logger.debug("kraken mail: connect with {}={}", s, props.get(s));
		}

		Authenticator auth = new SmtpAuthenticator(user, password);
		return Session.getInstance(props, auth);
	}
	
	public static void main(String[] args) throws MessagingException {
		Authenticator auth = new SmtpAuthenticator("xeraph@nchovy.com", "zkakdpf;");
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", 587);
		props.put("mail.smtp.user", "xeraph@nchovy.com");
		props.put("mail.smtp.password", "zkakdpf;");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		
		Session s = Session.getDefaultInstance(props, auth);
		MimeMessage msg = new MimeMessage(s);

		InternetAddress fromAddr = new InternetAddress("xeraph@nchovy.com");
		InternetAddress toAddr = new InternetAddress("delmitz@nchovy.com");

		msg.setFrom(fromAddr);
		msg.setRecipient(RecipientType.TO, toAddr);
		msg.setSubject("ahahahahaha");
		msg.setContent("qoo", "text/plain; charset=utf-8");

		Transport.send(msg);
	}

	private Preferences getPreferences() {
		return prefsvc.getSystemPreferences().node("smtp");
	}

	@Override
	public void register(String name, Properties props) {
		try {
			Preferences root = getPreferences();
			if (root.nodeExists(name))
				throw new IllegalStateException("duplicated smtp configuration name found");

			Preferences p = root.node(name);

			for (Object key : props.keySet()) {
				String k = key.toString();
				p.put(k, props.getProperty(k));
			}

			p.flush();
			p.sync();
		} catch (BackingStoreException e) {
			logger.warn("kraken-mail: cannot add config", e);
			throw new IllegalStateException(e.getMessage());
		}
	}

	@Override
	public void unregister(String name) {
		try {
			Preferences root = getPreferences();
			if (!root.nodeExists(name))
				throw new IllegalStateException("smtp config not found: " + name);

			root.node(name).removeNode();
			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			logger.warn("kraken-mail: cannot remove config", e);
			throw new IllegalStateException(e.getMessage());
		}
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

}
