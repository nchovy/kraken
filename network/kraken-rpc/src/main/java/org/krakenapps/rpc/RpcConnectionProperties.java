/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.rpc;

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public class RpcConnectionProperties {
	private String host;
	private int port;
	private TrustManagerFactory tmf;
	private KeyManagerFactory kmf;
	private X509Certificate peerCert;
	private String password;

	public RpcConnectionProperties(String host, int port) {
		this(host, port, null, null);
	}

	public RpcConnectionProperties(InetSocketAddress remote) {
		this(remote.getAddress().getHostAddress(), remote.getPort(), null, null);
	}

	public RpcConnectionProperties(InetSocketAddress remote, KeyManagerFactory kmf, TrustManagerFactory tmf) {
		this(remote.getAddress().getHostAddress(), remote.getPort(), kmf, tmf);
	}

	public RpcConnectionProperties(String host, int port, KeyManagerFactory kmf, TrustManagerFactory tmf) {
		this.host = host;
		this.port = port;
		this.kmf = kmf;
		this.tmf = tmf;
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

	public KeyManagerFactory getKeyManagerFactory() {
		return kmf;
	}

	public TrustManagerFactory getTrustManagerFactory() {
		return tmf;
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
