package org.krakenapps.mail;

import java.util.Properties;

import org.krakenapps.api.FieldOption;
import org.krakenapps.confdb.CollectionName;

@CollectionName("mailer")
public class MailerConfig {
	@FieldOption(nullable = false)
	private String name;
	@FieldOption(nullable = false)
	private String protocol;
	@FieldOption(nullable = false)
	private String host;
	@FieldOption(nullable = false)
	private int port;
	@FieldOption(nullable = false)
	private String user;
	@FieldOption(nullable = false)
	private String password;
	@FieldOption(nullable = false)
	private boolean auth;
	@FieldOption(nullable = false)
	private boolean tls;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isAuth() {
		return auth;
	}

	public void setAuth(boolean auth) {
		this.auth = auth;
	}

	public boolean isTls() {
		return tls;
	}

	public void setTls(boolean tls) {
		this.tls = tls;
	}

	public Properties getProperties() {
		Properties props = new Properties();
		props.put("mail.transport.protocol", protocol);
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", Integer.toString(port));
		props.put("mail.smtp.user", user);
		props.put("mail.smtp.password", password);
		props.put("mail.smtp.auth", Boolean.toString(auth));
		props.put("mail.smtp.starttls.enable", Boolean.toString(tls));
		return props;
	}

	@Override
	public String toString() {
		return String.format("[%s]%s, %s:%d\tuser: %s", name, protocol, host, port, user);
	}
}
