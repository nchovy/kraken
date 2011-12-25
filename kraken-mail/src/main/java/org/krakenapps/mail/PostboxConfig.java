package org.krakenapps.mail;

import java.util.Properties;

import org.krakenapps.confdb.CollectionName;

@CollectionName("postbox")
public class PostboxConfig {
	private String name;
	private String host;
	private int port;
	private String user;
	private String password;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Properties getProperties() {
		Properties prop = new Properties();
		prop.put("mail.imap.host", host);
		prop.put("mail.imap.port", Integer.toString(port));
		prop.put("mail.imap.user", user);
		prop.put("mail.imap.password", password);
		return prop;
	}
}
