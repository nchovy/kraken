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
package org.krakenapps.pcap.decoder.http.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

import org.krakenapps.pcap.decoder.http.HttpHeaders;
import org.krakenapps.pcap.decoder.http.HttpMethod;
import org.krakenapps.pcap.decoder.http.HttpRequest;
import org.krakenapps.pcap.decoder.http.HttpVersion;

/**
 * @author mindori
 */
public class HttpRequestImpl implements HttpRequest {
	private MimeMessage message;

	// connection metadata
	private InetSocketAddress client;
	private InetSocketAddress server;

	private HttpMethod method;
	private String path;
	private String queryString;
	private HttpVersion httpVersion;

	private Map<String, String> headers;
	private Map<String, String> parameters;

	// multipart variable
	private byte[] endBoundary;

	private Map<String, InputStream> files;

	public HttpRequestImpl(InetSocketAddress client, InetSocketAddress server) {
		this.client = client;
		this.server = server;

		headers = new HashMap<String, String>();
		parameters = new HashMap<String, String>();
		files = new HashMap<String, InputStream>();
	}

	@Override
	public MimeMessage getMimeMessage() {
		return message;
	}

	public void setMimeMessage(MimeMessage message) {
		this.message = message;
		try {
			extractFiles();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return client;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return server;
	}

	@Override
	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(String method) {
		if (method.equals("OPTIONS"))
			this.method = HttpMethod.OPTIONS;
		else if (method.equals("GET"))
			this.method = HttpMethod.GET;
		else if (method.equals("HEAD"))
			this.method = HttpMethod.HEAD;
		else if (method.equals("POST"))
			this.method = HttpMethod.POST;
		else if (method.equals("PUT"))
			this.method = HttpMethod.PUT;
		else if (method.equals("DELETE"))
			this.method = HttpMethod.DELETE;
		else if (method.equals("TRACE"))
			this.method = HttpMethod.TRACE;
		else if (method.equals("CONNECT"))
			this.method = HttpMethod.CONNECT;
	}

	public URL getURL() {
		String host = headers.get("Host").replaceAll("\n", "");
		if (host == null)
			host = server.getAddress().toString().substring(1);

		try {
			return new URI("http", host, path, queryString, null).toURL();
		} catch (URISyntaxException e) {
		} catch (MalformedURLException e) {
			// ignore all exceptions. must not reach here.
		}

		return null;
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	public void setPath(String path) {
		this.path = path;
		int queryStrOffset = path.indexOf("?");
		if (queryStrOffset != -1) {
			this.path = path.substring(0, queryStrOffset);
			queryString = path.substring(queryStrOffset + 1);
			setParameters();
		} else
			queryString = null;
	}

	public Set<String> getParameterKeys() {
		return parameters.keySet();
	}

	@Override
	public boolean containsParameter(String key) {
		return parameters.containsKey(key);
	}

	@Override
	public String getParameter(String key) {
		if (parameters.containsKey(key)) {
			return parameters.get(key);
		}
		return null;
	}

	public void addParameter(String key, String value) {
		parameters.put(key, value);
	}

	private void setParameters() {
		String[] params = queryString.split("&");
		for (String param : params) {
			String[] token = param.split("=");
			if (token.length == 2)
				parameters.put(token[0], token[1]);
			else if (token.length == 1)
				parameters.put(token[0], null);
		}
	}

	@Override
	public HttpVersion getHttpVersion() {
		return httpVersion;
	}

	public void setHttpVersion(String httpVersion) {
		if (httpVersion.equals("HTTP/1.1"))
			this.httpVersion = HttpVersion.HTTP_1_1;
		else
			this.httpVersion = HttpVersion.HTTP_1_0;
	}

	@Override
	public Set<String> getHeaderKeys() {
		return headers.keySet();
	}

	@Override
	public boolean containsHeader(String name) {
		return headers.containsKey(name);
	}

	@Override
	public String getHeader(String name) {
		if (headers.containsKey(name))
			return headers.get(name);
		return null;
	}

	public void addHeader(String header) {
		String[] token = header.split(": ");
		String headerName = HttpHeaders.canonicalize(token[0]);

		if(token.length <= 1) {
			headers.put(headerName, "");
		}
		else if( token[1] == null ) {
			headers.put(headerName, "");
		}
		else {
			headers.put(headerName, token[1]);
		}
	}

	public byte[] getEndBoundary() {
		return endBoundary;
	}

	public void setEndBoundary(byte[] endBoundary) {
		this.endBoundary = endBoundary;
	}

	@Override
	public String getTextContent() {
		try {
			if (message.getContent() instanceof String)
				return (String) message.getContent();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public Set<String> getFileNames() {
		return files.keySet();
	}

	@Override
	public InputStream getFile(String fileName) {
		return files.get(fileName);
	}

	private void extractFiles() throws IOException, MessagingException {
		if (message.getContent() instanceof Multipart) {
			Multipart mp = (Multipart) message.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				BodyPart bp = mp.getBodyPart(i);
				getMultipart(bp, bp.getContent());
			}
		}
	}

	private void getMultipart(BodyPart bp, Object content) throws IOException, MessagingException {
		if (!(content instanceof Multipart)) {
			if (bp.getFileName() != null && content instanceof InputStream) {
				files.put(bp.getFileName(), (InputStream) content);
			}
			return;
		}

		Multipart mp = (Multipart) content;
		for (int i = 0; i < mp.getCount(); i++) {
			BodyPart newBp = mp.getBodyPart(i);
			getMultipart(newBp, newBp.getContent());
		}
	}
}
