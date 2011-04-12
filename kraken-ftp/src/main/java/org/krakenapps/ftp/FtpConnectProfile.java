package org.krakenapps.ftp;

public class FtpConnectProfile {
	private String name;
	private String host;
	private int port;
	private String account;
	private String password;

	public FtpConnectProfile(String name, String host, int port, String account, String password) {
		this.name = name;
		this.host = host;
		this.port = port;
		this.account = account;
		this.password = password;
	}

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

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "name=" + name + ", host=" + host + ", port=" + port + ", account=" + account;
	}
}
