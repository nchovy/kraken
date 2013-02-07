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
 package org.krakenapps.sslscan;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import sun.security.validator.ValidatorException;

@SuppressWarnings("restriction")
public class SslScanner {
	private SSLContext ctx;

	public static void main(String[] args) throws Exception {
		new SslScanner().run(args);
	}

	public void run(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("SSL Cipher Suite Scanner, xeraph@nchovy.com");
			System.out.println("Usage: java -jar kraken-sslscan.jar [hostname] [port]");
			return;
		}

		ctx = SSLContext.getDefault();
		String hostname = args[0];
		Integer port = Integer.valueOf(args[1]);

		checkCertificate(hostname, port);
		checkAllCipherSuites(ctx, hostname, port);
	}

	private void checkCertificate(String hostname, int port) throws Exception {
		try {
			checkCipherSuite(hostname, port, null);
		} catch (SSLHandshakeException e) {
			if (e.getCause() instanceof ValidatorException) {
				System.out.println("Warning: Invalid Certificate, Ignoring..");
				System.out.println(">> " + e.getCause().getMessage());

				ctx = SSLContext.getInstance("SSL");
				ctx.init(null, trustAllCerts, new SecureRandom());
			}
		}
	}

	private void checkAllCipherSuites(SSLContext ctx, String hostname, Integer port) throws NoSuchAlgorithmException {
		SSLParameters sslParams = ctx.getSupportedSSLParameters();
		for (String cipher : sslParams.getCipherSuites()) {
			try {
				checkCipherSuite(hostname, port, cipher);
				System.out.println("PASS " + cipher);
			} catch (IOException e) {
				System.out.println("FAIL " + cipher);
			}
		}
	}

	public void checkCipherSuite(String hostname, int port, String cipher) throws IOException {
		SocketFactory socketFactory = ctx.getSocketFactory();
		Socket socket = socketFactory.createSocket(hostname, port);
		SSLSocket sslSocket = (SSLSocket) socket;

		if (cipher != null)
			sslSocket.setEnabledCipherSuites(new String[] { cipher });

		sslSocket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
			@Override
			public void handshakeCompleted(HandshakeCompletedEvent e) {
			}
		});

		sslSocket.startHandshake();
	}

	TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
	} };

}
