package org.krakenapps.mail;

import java.util.Collection;

import javax.mail.MessagingException;
import javax.mail.Session;

public interface MailerRegistry {
	Collection<MailerConfig> getConfigs();

	MailerConfig getConfig(String name);

	void register(MailerConfig config);

	void unregister(String name);

	Session getSession(MailerConfig config);

	void send(MailerConfig config, String from, String to, String subject, String message) throws MessagingException;
}
