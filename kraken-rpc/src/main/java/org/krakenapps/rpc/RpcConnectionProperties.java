package org.krakenapps.rpc;

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;

public class RpcConnectionProperties {
	private String host;
	private int port;
	private String keyAlias;
	private String trustAlias;

	private X509Certificate peerCert;
	private String password;

	public RpcConnectionProperties(String host, int port) {
		this(host, port, "rpc-agent", "rpc-ca");
	}

	public RpcConnectionProperties(String host, int port, String keyAlias, String trustAlias) {
		this.host = host;
		this.port = port;
		this.keyAlias = keyAlias;
		this.trustAlias = trustAlias;
	}
	
	public InetSocketAddress getRemoteAddress() {
		return new InetSocketAddress(getHost(), getPort());
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getKeyAlias() {
		return keyAlias;
	}

	public String getTrustAlias() {
		return trustAlias;
	}

	public X509Certificate getPeerCert() {
		return peerCert;
	}

	public void setPeerCert(X509Certificate peerCert) {
		this.peerCert = peerCert;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
