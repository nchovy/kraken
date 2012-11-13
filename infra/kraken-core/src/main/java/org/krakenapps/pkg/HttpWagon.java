/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.pkg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.main.Kraken;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpWagon {
	private HttpWagon() {
	}

	public static String downloadString(URL url) throws IOException {
		return downloadString(url, false, null, null);
	}

	public static String downloadString(URL url, String username, String password) throws IOException {
		return downloadString(url, true, username, password);
	}

	private static String downloadString(URL url, boolean useAuth, String username, String password) throws IOException {
		byte[] responseBody = download(url, useAuth, username, password);
		return new String(responseBody, Charset.forName("utf-8"));
	}

	public static byte[] download(URL url) throws IOException {
		return download(url, false, null, null);
	}

	public static InputStream openDownloadStream(URL url) throws IOException {
		return openDownloadStream(url, false, null, null);
	}

	public static byte[] download(URL url, TrustManagerFactory tmf, KeyManagerFactory kmf) throws KeyManagementException,
			IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		InputStream is = openDownloadStream(url, tmf, kmf);
		try {
			byte[] b = new byte[8096];
			while (true) {
				int read = is.read(b);
				if (read <= 0)
					break;
				os.write(b, 0, read);
			}
			return os.toByteArray();
		} finally {
			is.close();
		}
	}

	public static InputStream openDownloadStream(URL url, TrustManagerFactory tmf, KeyManagerFactory kmf)
			throws KeyManagementException, IOException {
		SSLContext ctx = null;
		try {
			ctx = SSLContext.getInstance("SSL");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		TrustManager[] trustManagers = null;
		KeyManager[] keyManagers = null;
		if (tmf != null)
			trustManagers = tmf.getTrustManagers();
		if (kmf != null)
			keyManagers = kmf.getKeyManagers();

		ctx.init(keyManagers, trustManagers, new SecureRandom());

		HttpsSocketFactory h = new HttpsSocketFactory(kmf, tmf);
		Protocol https = new Protocol("https", (ProtocolSocketFactory) h, 443);
		Protocol.registerProtocol("https", https);

		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(url.toString());
		client.executeMethod(method);
		return method.getResponseBodyAsStream();
	}

	public static byte[] download(URL url, boolean useAuth, String username, String password) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		InputStream is = openDownloadStream(url, useAuth, username, password);
		try {
			byte[] b = new byte[8096];
			while (true) {
				int read = is.read(b);
				if (read <= 0)
					break;
				os.write(b, 0, read);
			}
			return os.toByteArray();
		} finally {
			is.close();
		}
	}

	public static InputStream openDownloadStream(URL url, boolean useAuth, String username, String password) throws IOException {
		Logger logger = LoggerFactory.getLogger(HttpWagon.class.getName());
		logger.trace("http wagon: downloading {}", url);

		HttpClient client = new HttpClient();
		if (useAuth) {
			client.getParams().setAuthenticationPreemptive(true);
			Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
			client.getState().setCredentials(new AuthScope(url.getHost(), url.getPort(), AuthScope.ANY_REALM), defaultcreds);
		}

		HttpMethod method = new GetMethod(url.toString());

		int socketTimeout = getSocketTimeout();
		int connectionTimeout = getConnectTimeout();

		client.getParams().setParameter("http.socket.timeout", socketTimeout);
		client.getParams().setParameter("http.connection.timeout", connectionTimeout);
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, null);

		int statusCode = client.executeMethod(method);

		if (statusCode != HttpStatus.SC_OK) {
			throw new IOException("method failed: " + method.getStatusLine());
		}

		return method.getResponseBodyAsStream();
	}

	private static int getSocketTimeout() {
		ServiceReference ref = Kraken.getContext().getServiceReference(ConfigService.class.getName());
		if (ref != null) {
			ConfigService conf = (ConfigService) Kraken.getContext().getService(ref);
			ConfigDatabase db = conf.ensureDatabase("kraken-core");
			Config c = db.findOne(HttpWagonConfig.class, null);
			if (c != null) {
				HttpWagonConfig hc = c.getDocument(HttpWagonConfig.class);
				return hc.getReadTimeout();
			}
		}
		return 10000;
	}

	private static int getConnectTimeout() {
		ServiceReference ref = Kraken.getContext().getServiceReference(ConfigService.class.getName());
		if (ref != null) {
			ConfigService conf = (ConfigService) Kraken.getContext().getService(ref);
			ConfigDatabase db = conf.ensureDatabase("kraken-core");
			Config c = db.findOne(HttpWagonConfig.class, null);
			if (c != null) {
				HttpWagonConfig hc = c.getDocument(HttpWagonConfig.class);
				return hc.getConnectTimeout();
			}
		}
		return 10000;
	}

	static class HttpsSocketFactory implements SecureProtocolSocketFactory, HandshakeCompletedListener {
		private KeyManagerFactory kmf;
		private TrustManagerFactory tmf;

		public HttpsSocketFactory(KeyManagerFactory kmf, TrustManagerFactory tmf) {
			this.kmf = kmf;
			this.tmf = tmf;
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
				UnknownHostException {
			return createSocket(host, port);
		}

		@Override
		public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort, HttpConnectionParams params)
				throws IOException, UnknownHostException, ConnectTimeoutException {
			return createSocket(host, port);
		}

		@Override
		public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException,
				UnknownHostException {
			return createSocket(host, port);
		}

		@Override
		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			try {
				KeyManager[] keyManagers = null;
				if (kmf != null)
					keyManagers = kmf.getKeyManagers();

				TrustManager[] trustManagers = null;
				if (tmf != null)
					trustManagers = tmf.getTrustManagers();

				SSLContext ctx = SSLContext.getInstance("SSL");
				ctx.init(keyManagers, trustManagers, new SecureRandom());
				SSLSocket socket = (SSLSocket) ctx.getSocketFactory().createSocket(host, port);
				socket.setNeedClientAuth(true);
				socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
				socket.addHandshakeCompletedListener(this);
				return socket;
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (KeyManagementException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		public void handshakeCompleted(HandshakeCompletedEvent event) {
			Logger logger = LoggerFactory.getLogger(HttpWagon.class);

			try {
				StringBuilder sb = new StringBuilder(4096);
				SSLSession session = event.getSession();
				sb.append(String.format("cipher %s, protocol %s, peer %s, ", event.getCipherSuite(), session.getProtocol(),
						session.getPeerHost()));

				java.security.cert.Certificate[] certs = event.getPeerCertificates();
				for (int i = 0; i < certs.length; i++) {
					if (!(certs[i] instanceof java.security.cert.X509Certificate))
						continue;

					java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) certs[i];
					System.out.println("Cert #" + i + ": " + cert.getSubjectDN().getName());
					sb.append("\ncert #" + i + ": " + cert.getSubjectDN().getName());
				}

				logger.debug("kraken core: handshake completed, {}", sb.toString());
			} catch (Exception e) {
				logger.error("kraken core: handshake complete parse error", e);
			}
		}
	}
}
