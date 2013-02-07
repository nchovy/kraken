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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.SharedByteArrayInputStream;

import org.krakenapps.pcap.decoder.http.HttpHeaders;
import org.krakenapps.pcap.decoder.http.HttpResponse;
import org.krakenapps.pcap.decoder.http.HttpVersion;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindori
 */
public class HttpResponseImpl implements HttpResponse {
	private final Logger logger = LoggerFactory.getLogger(HttpResponseImpl.class.getName());

	private Buffer binary;

	private HttpVersion httpVersion;
	private int statusCode;
	private String reasonPhrase;
	private Map<String, String> headers;

	/* flags represent to content type of http */
	private EnumSet<FlagEnum> flags = EnumSet.of(FlagEnum.NONE);

	/* NORMAL variable */
	private int putLength = 0;

	/* MULTIPART, BYTERANGE variable */
	private String boundary;
	private int partLength = -1;

	/* GZIP variable */
	private int gzipOffset = 0;
	private int gzipLength = -1;

	/* CHUNKED variable */
	private int chunkedOffset = 0;
	private int chunkedLength = -1;

	private Buffer contentBuffer;
	private Buffer gzipBuf;
	private List<Byte> gzipContent;
	private List<Byte> chunked;
	private Buffer chunkedBuf;

	// private String contentStr;
	private byte[] content;
	private byte[] decompressedGzip;
	private byte[] chunkedBytes;

	private String textContent;
	private InputStream inputStream;

	private MimeMessage message;

	public HttpResponseImpl() {
		binary = new ChainBuffer();
		headers = new HashMap<String, String>();
	}

	public void putBinary(Buffer data) {
		binary.addLast(data);
	}

	public Buffer getBinary() {
		return binary;
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
	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public String getStatusLine() {
		return statusCode + " " + reasonPhrase;
	}

	public void setReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	@Override
	public Set<String> getHeaderKeys() {
		return headers.keySet();
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

	public EnumSet<FlagEnum> getFlag() {
		return flags;
	}

	public int getPutLength() {
		return putLength;
	}

	public void addPutLength(int putLength) {
		this.putLength += putLength;
	}

	public String getBoundary() {
		return boundary;
	}

	public void setBoundary(String boundary) {
		this.boundary = boundary;
	}

	public int getPartLength() {
		return partLength;
	}

	public void setPartLength(int partLength) {
		this.partLength = partLength;
	}

	public int getGzipOffset() {
		return gzipOffset;
	}

	public void setGzipOffset(int gzipOffset) {
		this.gzipOffset = gzipOffset;
	}

	public int getGzipLength() {
		return gzipLength;
	}

	public void setGzipLength(int gzipLength) {
		this.gzipLength = gzipLength;
	}

	public int getChunkedOffset() {
		return chunkedOffset;
	}

	public void setChunkedOffset(int chunkedOffset) {
		this.chunkedOffset = chunkedOffset;
	}

	public int getChunkedLength() {
		return chunkedLength;
	}

	public void setChunkedLength(int chunkedLength) {
		this.chunkedLength = chunkedLength;
	}

	public void createContent() {
		contentBuffer = new ChainBuffer();
	}

	public Buffer getContentBuffer() {
		return contentBuffer;
	}

	public void createGzip() {
		gzipContent = new ArrayList<Byte>();
	}

	public List<Byte> getGzip() {
		return gzipContent;
	}

	public void putGzip(byte b) {
		gzipContent.add(b);
	}

	public void putGzip(List<Byte> b) {
		gzipContent.addAll(b);
	}

	public Buffer getGzipBuf() {
		return gzipBuf;
	}

	public void putGzipBuf(byte[] b) {
		gzipBuf.addLast(b);
	}

	public void createChunked() {
		chunked = new ArrayList<Byte>();
	}

	public List<Byte> getChunked() {
		return chunked;
	}

	public void putChunked(List<Byte> bList) {
		chunked.addAll(chunkedOffset, bList);
	}

	public Buffer getChunkedBuf() {
		return chunkedBuf;
	}

	public void putChunkedBuf(byte[] b) {
		chunkedBuf.addLast(b);
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public void setDecompressedGzip(byte[] decompressedGzip) {
		this.decompressedGzip = decompressedGzip;
	}

	public void setChunked(byte[] chunkedBytes) {
		this.chunkedBytes = chunkedBytes;
	}

	public MimeMessage getMimeMessage() {
		return message;
	}

	public void setMessage(MimeMessage message) {
		this.message = message;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	@Override
	public String getContent() {
		return textContent;
	}

	public void setContent() {
		String type = headers.get(HttpHeaders.CONTENT_TYPE);
		String charset = null;

		/* try to extract character set from 'Content-Type' field */
		if (type != null) {
			int charsetPos = type.indexOf("charset=");
			int boundary = type.indexOf(";");

			if (charsetPos != -1)
				charset = type.substring(charsetPos + 8);
			if (boundary != -1)
				type = type.substring(0, boundary);
		}

		mappingContents(type, charset);
	}

	private void mappingContents(String type, String charset) {
		if (compareContentType(type)) {
			if (flags.contains(FlagEnum.GZIP)) {
				try {
					if (decompressedGzip == null) {
						/* decompress failed */
						if (logger.isDebugEnabled())
							logger.debug("kraken http decoder: gzip decoding failed");
						textContent = null;
					} else {
						if (charset != null)
							textContent = new String(decompressedGzip, charset);
						else {
							Charset ch = extractCharset(decompressedGzip);
							if (ch != null)
								textContent = new String(decompressedGzip, ch);
							else
								textContent = new String(decompressedGzip, Charset.defaultCharset());
						}

					}
				} catch (UnsupportedEncodingException e) {
					if (logger.isDebugEnabled())
						logger.debug("kraken http decoder: unsupported encoding", e);
				}
			} else if (flags.contains(FlagEnum.CHUNKED)) {
				try {
					/* added code */
					if (message.getContent() instanceof SharedByteArrayInputStream) {
						inputStream = new ByteArrayInputStream(chunkedBytes);
					}
					/* added code end */
					if (charset != null)
						textContent = new String(chunkedBytes, charset);
					else {
						Charset ch = extractCharset(chunkedBytes);
						if (ch != null)
							textContent = new String(chunkedBytes, ch);
						else
							textContent = new String(chunkedBytes, Charset.defaultCharset());
					}
				} catch (UnsupportedEncodingException e) {
					if (logger.isDebugEnabled())
						logger.debug("kraken http decoder: unsupported encoding", e);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			} else if (flags.contains(FlagEnum.BYTERANGE)) {
				if (content != null)
					textContent = new String(content);
			} else if (flags.contains(FlagEnum.NORMAL)) {
				try {
					if (content == null)
						return;

					if (charset != null)
						textContent = new String(content, charset);
					else {
						Charset ch = extractCharset(content);
						if (ch != null)
							textContent = new String(content, ch);
						else
							textContent = new String(content, Charset.defaultCharset());
					}
				} catch (UnsupportedEncodingException e) {
					if (logger.isDebugEnabled())
						logger.debug("kraken http decoder: unsupported encoding", e);
				}
			}
		}
	}

	/* try to extract character set from <META> tag */
	private Charset extractCharset(byte[] content) {
		if (content == null)
			return null;

		String s;

		if (content.length > 1024)
			s = new String(content, 0, 1024);
		else if (content.length > 200)
			s = new String(content, 0, 200);
		else
			return null;

		/* avoid upper case characters */
		s = s.toLowerCase();

		String charset = parseCharset(s, "<meta");
		if (charset == null) {
			charset = parseCharset(s, "<script");
			if (charset == null) {
				charset = parseCharsetFromCss(s);
			}
		}

		if (charset != null) {
			try {
				return Charset.forName(charset.replaceAll("\"", "").replaceAll("/", "").trim());
			} catch (IllegalCharsetNameException e) {
				return null;
			}
		}

		return null;
	}

	private String parseCharset(String content, String indexStr) {
		int i = content.indexOf(indexStr);
		if (i != -1) {
			int j = content.indexOf("charset");
			if (j != -1) {
				int k = j + 8;
				while (k < content.length()) {
					if (content.charAt(k) == '"' || content.charAt(k) == '>')
						break;
					k++;
				}

				return content.substring(j + 8, k);
			}
		}
		return null;
	}

	private String parseCharsetFromCss(String content) {
		int j = content.indexOf("@charset");
		if (j != -1) {
			int k = j + 10;
			while (k < content.length()) {
				if (content.charAt(k) == '"')
					break;
				k++;
			}

			return content.substring(j + 10, k);
		}
		return null;
	}

	private boolean compareContentType(String type) {
		if(type == null)
			return false;
		
		List<String> contentTypes = new ArrayList<String>();

		contentTypes.add("text/css");
		contentTypes.add("text/html");
		contentTypes.add("text/javascript");
		contentTypes.add("text/plain");
		contentTypes.add("text/xml");
		contentTypes.add("application/x-javascript");
		contentTypes.add("application/javascript");
		contentTypes.add("application/xml");
		contentTypes.add("application/octet-stream");

		return contentTypes.contains(type);
	}
}