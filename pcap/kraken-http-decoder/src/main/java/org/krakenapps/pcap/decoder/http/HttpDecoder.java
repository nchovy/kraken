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
package org.krakenapps.pcap.decoder.http;

import java.io.ByteArrayInputStream;

import java.nio.BufferUnderflowException;
import java.nio.charset.Charset;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.krakenapps.pcap.decoder.http.impl.FlagEnum;
import org.krakenapps.pcap.decoder.http.impl.HttpRequestImpl;
import org.krakenapps.pcap.decoder.http.impl.HttpRequestState;
import org.krakenapps.pcap.decoder.http.impl.HttpResponseImpl;
import org.krakenapps.pcap.decoder.http.impl.HttpResponseState;
import org.krakenapps.pcap.decoder.http.impl.HttpSession;
import org.krakenapps.pcap.decoder.http.impl.PartialContentManager;
import org.krakenapps.pcap.decoder.tcp.TcpProcessor;
import org.krakenapps.pcap.decoder.tcp.TcpSessionKey;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

enum HttpDirection {
	REQUEST, RESPONSE
};

/**
 * @author mindori
 */
public class HttpDecoder implements TcpProcessor {
	private static final int DECODE_NOT_READY = -1;
	private Logger logger = LoggerFactory.getLogger(HttpDecoder.class.getName());

	private Set<HttpProcessor> callbacks;
	private Map<TcpSessionKey, HttpSession> sessionMap;

	private PartialContentManager mpManager;

	public HttpDecoder() {
		callbacks = new HashSet<HttpProcessor>();
		sessionMap = new HashMap<TcpSessionKey, HttpSession>();
		mpManager = new PartialContentManager();
	}

	public void register(HttpProcessor processor) {
		callbacks.add(processor);
	}

	public void unregister(HttpProcessor processor) {
		callbacks.remove(processor);
	}

	@Override
	public void handleTx(TcpSessionKey sessionKey, Buffer data) {
		HttpSession session = sessionMap.get(sessionKey);
		handleRequest(session, data);
	}

	@Override
	public void handleRx(TcpSessionKey sessionKey, Buffer data) {
		HttpSession session = sessionMap.get(sessionKey);
		handleResponse(session, data);
	}

	@Override
	public void onEstablish(TcpSessionKey session) {
		if (logger.isDebugEnabled())
			logger.debug("-> Http Session Established: " + (int) session.getClientPort() + " -> " + (int) session.getServerPort());
		InetAddress clientIp = session.getClientIp();
		InetAddress serverIp = session.getServerIp();
		InetSocketAddress clientAddr = new InetSocketAddress(clientIp, session.getClientPort());
		InetSocketAddress serverAddr = new InetSocketAddress(serverIp, session.getServerPort());
		sessionMap.put(session, new HttpSession(clientAddr, serverAddr));
	}

	@Override
	public void onFinish(TcpSessionKey session) {
		HttpSession httpSession = sessionMap.remove(session);
		handleNoContentLengthCase(httpSession);

		if (logger.isDebugEnabled())
			logger.debug("-> Http Session Closed: \n" + "Client Port: " + (int) session.getClientPort() + "\nServer Port: " + (int) session.getServerPort());
	}

	@Override
	public void onReset(TcpSessionKey session) {
		HttpSession httpSession = sessionMap.remove(session);
		handleNoContentLengthCase(httpSession);

		if (httpSession == null)
			return;

		httpSession.deallocate();

		if (logger.isDebugEnabled())
			logger.debug("Deallocate tx, rx buffer and remove Http session.");
	}

	private void handleNoContentLengthCase(HttpSession httpSession) {
		if (httpSession != null && httpSession.getResponseState() == HttpResponseState.GOT_HEADER) {
			decodeContent(httpSession.getResponse());
			dispatchResponse(httpSession);
		}
	}

	private void handleRequest(HttpSession session, Buffer data) {
		Buffer txBuffer = session.getTxBuffer();
		txBuffer.addLast(data);
		parseRequest(session, txBuffer);
	}

	private void handleResponse(HttpSession session, Buffer data) {
		int capacity = data.readableBytes();
		Buffer rxBuffer = session.getRxBuffer();
		rxBuffer.addLast(data);
		parseResponse(session, rxBuffer, data, capacity);
	}

	private void parseRequest(HttpSession session, Buffer txBuffer) {
		if (session.getRequest() == null)
			session.createRequest();

		HttpRequestImpl request = session.getRequest();

		/* multiple requests in a session. */
		if (session.getRequestState() == HttpRequestState.END)
			session.setRequestState(HttpRequestState.READY);

		while (session.getRequestState() != HttpRequestState.END) {
			switch (session.getRequestState()) {
			case READY:
			case GOT_METHOD:
				try {
					int len = txBuffer.bytesBefore(new byte[] { 0x20 });
					if (len == 0) {
						return;
					}

					byte[] t = new byte[len];
					txBuffer.gets(t);

					/* skip space */
					txBuffer.get();

					if (session.getRequestState() == HttpRequestState.READY) {
						request.setMethod(new String(t));
						session.setRequestState(HttpRequestState.GOT_METHOD);
					} else {
						request.setPath(new String(t));
						session.setRequestState(HttpRequestState.GOT_URI);
					}

				} catch (BufferUnderflowException e) {
					txBuffer.reset();
					return;
				}
				break;

			case GOT_URI:
				try {
					int len = txBuffer.bytesBefore(new byte[] { 0x0d, 0x0a });
					if (len == 0) {
						return;
					}

					byte[] t = new byte[len];
					txBuffer.gets(t);

					/* skip \r\n */
					txBuffer.get();
					txBuffer.get();

					request.setHttpVersion(new String(t));
					session.setRequestState(HttpRequestState.GOT_HTTP_VER);
				} catch (BufferUnderflowException e) {
					txBuffer.reset();
					return;
				}
				break;

			case GOT_HTTP_VER:
				try {
					int len = txBuffer.bytesBefore(new byte[] { 0x0d, 0x0a });
					if (len == 0) {
						return;
					}

					byte[] t = new byte[len];
					txBuffer.gets(t);

					txBuffer.get();
					txBuffer.get();

					request.addHeader(new String(t));

					txBuffer.mark();
					byte s2 = txBuffer.get();
					byte s3 = txBuffer.get();
					if (s2 == 0x0d && s3 == 0x0a)
						session.setRequestState(HttpRequestState.GOT_HEADER);
					else
						txBuffer.reset();
				} catch (BufferUnderflowException e) {
					txBuffer.reset();
					return;
				}
				break;

			case GOT_HEADER:
				if (request.containsHeader(HttpHeaders.CONTENT_LENGTH)) {
					int contentLength = Integer.valueOf(request.getHeader(HttpHeaders.CONTENT_LENGTH));
					if (txBuffer.readableBytes() < contentLength)
						return;

					// read request body
					byte[] body = new byte[txBuffer.readableBytes()];
					txBuffer.gets(body);
					parseRequestBody(request, body);
				}

				dispatchRequest(request);
				session.setRequestState(HttpRequestState.END);
				break;
			}
		}
	}

	private void parseRequestBody(HttpRequestImpl request, byte[] body) {
		if (request.containsHeader(HttpHeaders.CONTENT_TYPE)) {
			String[] tokens = request.getHeader(HttpHeaders.CONTENT_TYPE).split(";");
			if (tokens[0].equalsIgnoreCase("application/x-www-form-urlencoded")) {
				parseUrlEncodedParams(request, body, tokens);
			}
		}
	}

	private void parseUrlEncodedParams(HttpRequestImpl request, byte[] body, String[] tokens) {
		// determine body encoding
		String encoding = "utf-8";
		for (int i = 1; i < tokens.length; i++) {
			if (tokens[i].startsWith("charset="))
				encoding = tokens[i].substring("charset=".length());
		}

		// split parameters
		Charset charset = Charset.forName(encoding);
		String content = new String(body, charset);
		String[] args = content.split("&");
		for (String arg : args) {
			String[] pair = arg.split("=");
			try {
				String key = URLDecoder.decode(pair[0], encoding);
				String value = null;
				if (pair.length > 1)
					value = URLDecoder.decode(pair[1], encoding);
				request.addParameter(key, value);
			} catch (UnsupportedEncodingException e) {
			}
		}
	}

	private void parseResponse(HttpSession session, Buffer rxBuffer, Buffer data, int capacity) {
		if (session.getResponse() == null)
			session.createResponse();

		HttpResponseImpl response = session.getResponse();

		response.putBinary(data);
		response.addPutLength(data.readableBytes());

		/* multiple responses in a session. */
		if (session.getResponseState() == HttpResponseState.END)
			session.setResponseState(HttpResponseState.READY);

		while (session.getResponseState() != HttpResponseState.END) {
			switch (session.getResponseState()) {
			case READY:
			case GOT_HTTP_VER:
				try {
					int len = rxBuffer.bytesBefore(new byte[] { 0x20 });
					if (len == 0) {
						return;
					}

					byte[] t = new byte[len];
					rxBuffer.gets(t);

					rxBuffer.get();

					if (session.getResponseState() == HttpResponseState.READY) {
						response.setHttpVersion(new String(t));
						session.setResponseState(HttpResponseState.GOT_HTTP_VER);
					} else {
						response.setStatusCode(Integer.valueOf(new String(t)));
						session.setResponseState(HttpResponseState.GOT_STATUS_CODE);
					}
				} catch (BufferUnderflowException e) {
					rxBuffer.reset();
					return;
				}
				break;

			case GOT_STATUS_CODE:
				try {
					int len = rxBuffer.bytesBefore(new byte[] { 0x0d, 0x0a });
					if (len == 0) {
						return;
					}

					byte[] t = new byte[len];
					rxBuffer.gets(t);

					rxBuffer.get();
					rxBuffer.get();

					response.setReasonPhrase(new String(t));
					session.setResponseState(HttpResponseState.GOT_REASON_PHRASE);
				} catch (BufferUnderflowException e) {
					rxBuffer.reset();
					return;
				}
				break;

			case GOT_REASON_PHRASE:
				try {
					int len = rxBuffer.bytesBefore(new byte[] { 0x0d, 0x0a });
					if (len == 0) {
						return;
					}

					byte[] t = new byte[len];
					rxBuffer.gets(t);

					rxBuffer.get();
					rxBuffer.get();
					response.addHeader(new String(t));

					rxBuffer.mark();
					byte s2 = rxBuffer.get();
					byte s3 = rxBuffer.get();
					if (s2 == 0x0d && s3 == 0x0a)
						session.setResponseState(HttpResponseState.GOT_HEADER);
					else
						rxBuffer.reset();
				} catch (BufferUnderflowException e) {
					rxBuffer.reset();
					return;
				}
				break;

			case GOT_HEADER:
				/* Get body of response */
				EnumSet<FlagEnum> flag = response.getFlag();

				/* Classify response type */
				if ((flag.size() <= 1) && (flag.contains(FlagEnum.NONE))) {
					rxBuffer.mark();
					setResponseType(response);
				}

				if (flag.contains(FlagEnum.NORMAL)) {
					if (handleNormal(response, rxBuffer) == DECODE_NOT_READY) {
						rxBuffer.reset();
						return;
					} else {
						decodeContent(response);
					}
				}

				else {
					/* step 1. handle MULTIPART or BYTERANGE */
					if (flag.contains(FlagEnum.MULTIPART)) {
						handleMultipart(response, rxBuffer);
					} else if (flag.contains(FlagEnum.BYTERANGE)) {
						String url = session.getRequest().getURL().toString();
						if (handleByteRange(response, url, rxBuffer, data, capacity) == DECODE_NOT_READY)
							return;
					}

					/* step 2 */
					if (flag.contains(FlagEnum.CHUNKED)) {
						int retVal = handleChunked(response, rxBuffer);

						if (retVal == DECODE_NOT_READY) {
							rxBuffer.reset();
							return;
						} else if (retVal == 0) {
							return;
						} else {
							setChunked(response);
							/* added code */
							/*
							 * TODO: set MimeMessage object(temporarily), I'll fixed soon.
							 */
							Buffer binary = response.getBinary();
							int length = response.getPutLength();
							byte[] b = new byte[length];
							binary.gets(b, 0, length);

							Session session2 = Session.getDefaultInstance(new Properties());
							InputStream is = new ByteArrayInputStream(b, 0, b.length);
							MimeMessage msg;
							try {
								msg = new MimeMessage(session2, is);
								response.setMessage(msg);
							} catch (MessagingException e) {
								e.printStackTrace();
							}
							/* added code end */
						}
					}

					/* step 3 */
					if (flag.contains(FlagEnum.DEFLATE)) {
						handleDeflate(response, rxBuffer);
					} else if (flag.contains(FlagEnum.GZIP)) {
						int retVal;
						if (flag.contains(FlagEnum.CHUNKED)) {
							retVal = handleGzip(response, response.getChunked());
						} else {
							retVal = handleGzip(response, rxBuffer);
						}

						if (retVal == DECODE_NOT_READY || retVal == 1) {
							return;
						} else if (retVal == 0) {
							try {
								byte[] decompressed = decompressGzip(response.getGzip());
								response.setDecompressedGzip(decompressed);
							} catch (DataFormatException e) {
								response.setDecompressedGzip(null);
							}
						}
					}
				}

				dispatchResponse(session);
				session.setResponseState(HttpResponseState.END);
				session.removeHttpMessages();
				break;
			}
		}
	}

	private void setResponseType(HttpResponseImpl response) {
		EnumSet<FlagEnum> flags = response.getFlag();

		String range = response.getHeader(HttpHeaders.CONTENT_RANGE);
		if (range != null) {
			if (range.substring(0, 5).equals("bytes")) {
				flags.add(FlagEnum.BYTERANGE);
				return;
			}
		}

		String type1 = response.getHeader(HttpHeaders.CONTENT_TYPE);
		if (type1 != null) {
			if (type1.length() >= 20 && type1.substring(0, 20).equals("multipart/byteranges")) {
				flags.add(FlagEnum.BYTERANGE);
				return;
			} else if (type1.length() >= 9 && type1.substring(0, 9).equals("multipart")) {
				flags.add(FlagEnum.MULTIPART);
				return;
			}
		}

		String type2 = response.getHeader(HttpHeaders.TRANSFER_ENCODING);
		if (type2 != null) {
			if (type2.matches("^chunked")) {
				flags.add(FlagEnum.CHUNKED);
				response.createChunked();
			}
		}

		String type3 = response.getHeader(HttpHeaders.CONTENT_ENCODING);
		if (type3 != null) {
			if (type3.matches("^gzip")) {
				flags.add(FlagEnum.GZIP);
				response.createGzip();

				String lengthStr = response.getHeader(HttpHeaders.CONTENT_LENGTH);
				if (lengthStr != null)
					response.setGzipLength(Integer.parseInt(lengthStr.trim()));
				return;
			} else if (type3.matches("^deflate")) {
				flags.add(FlagEnum.DEFLATE);
				return;
			}
		}

		if ((flags.size() <= 1) && (flags.contains(FlagEnum.NONE))) {
			flags.add(FlagEnum.NORMAL);
			response.createContent();
		}
	}

	private void handleMultipart(HttpResponseImpl response, Buffer rxBuffer) {
	}

	private int handleByteRange(HttpResponseImpl response, String url, Buffer rxBuffer, Buffer data, int capacity) {
		String type = response.getHeader(HttpHeaders.CONTENT_TYPE);
		if(type == null)
			return DECODE_NOT_READY;
		
		if (type.length() > 20) {
			/* case 1: response's Content-Type is multipart/byteranges */
			if (response.getBoundary() == null) {
				if (type.substring(0, 20).equals("multipart/byteranges")) {
					int pos = type.indexOf("=");
					response.setBoundary(type.substring(pos + 1).replaceAll("\r", "").replaceAll("\n", ""));
				}
			}

			/* check reach endpoint */
			String endBoundary = "--" + response.getBoundary() + "--\r\n";
			byte[] b = new byte[endBoundary.length()];

			int j = capacity - endBoundary.length();
			data.mark();
			data.position(j);
			for (int i = 0; i < endBoundary.length(); i++) {
				b[i] = data.get();
			}
			data.reset();

			String makeBoundary = new String(b);
			if (endBoundary.equals(makeBoundary)) {
				parseMultipart(response, url, rxBuffer);
				return 0;
			}
		}
		/* case 2: response have a Content-Range */
		else {
			int partLength;
			if (response.getPartLength() == DECODE_NOT_READY)
				partLength = getPartLength(response);
			else
				partLength = response.getPartLength();

			int readable = rxBuffer.readableBytes();
			if (readable >= partLength) {
				byte[] t = new byte[readable];
				rxBuffer.gets(t);
				response.setContent(t);
				// response.setContentStr(new String(t));
				return 0;
			}
		}
		return DECODE_NOT_READY;
	}

	private int getPartLength(HttpResponseImpl response) {
		String range = response.getHeader(HttpHeaders.CONTENT_RANGE);
		if (range == null)
			return DECODE_NOT_READY;

		int pos = range.indexOf("bytes ");
		String[] ranges = range.substring(pos + 6).split("/")[0].split("-");
		int begin = Integer.parseInt(ranges[0]);
		int end = Integer.parseInt(ranges[1]);

		response.setPartLength(end - begin);
		return (end - begin);
	}

	private void parseMultipart(HttpResponseImpl response, String url, Buffer rxBuffer) {
		byte[] boundary = response.getBoundary().getBytes();

		try {
			byte b = 0;
			while (true) {
				b = rxBuffer.get();
				/* find boundary */
				if (!(b == 0x2d && rxBuffer.get() == 0x2d))
					continue;

				rxBuffer.mark();
				int k;
				for (k = 0; k < boundary.length; k++) {
					b = rxBuffer.get();

					if (b != boundary[k]) {
						rxBuffer.reset();
						break;
					}
				}
				if (k != boundary.length) {
					continue;
				}

				/* skip \r\n */
				rxBuffer.get();
				rxBuffer.get();

				boolean isGetRange = false;
				while (!isGetRange) {
					/* read bytes after boundary */
					int headerLen = 0;
					rxBuffer.mark();

					while (true) {
						b = rxBuffer.get();
						if (b == 0x3a || b == DECODE_NOT_READY)
							break;
						headerLen++;
					}

					rxBuffer.reset();
					byte[] hBytes = new byte[headerLen];
					rxBuffer.gets(hBytes);

					String header = new String(hBytes);
					if (header.equalsIgnoreCase("Content-Range")) {
						int l = 0;
						while (l < 8) {
							/* skip ': bytes ' */
							rxBuffer.get();
							l++;
						}

						List<Byte> bList = new ArrayList<Byte>();
						while (true) {
							b = rxBuffer.get();
							if (b == 0x0d)
								break;
							bList.add(b);
						}

						/* skip \r\n\r\n */
						rxBuffer.get();
						rxBuffer.get();
						rxBuffer.get();

						byte[] rangeBytes = new byte[bList.size()];
						for (int i = 0; i < rangeBytes.length; i++) {
							rangeBytes[i] = bList.get(i);
						}

						String range = new String(rangeBytes);
						String[] token = range.split("/");

						if (token.length <= 1) {
							isGetRange = true;
							continue;
						}

						String[] s = token[0].split("-");

						int first = Integer.parseInt(s[0]);
						int last = Integer.parseInt(s[1]);
						int length = last - first;
						int readOffset = 0;

						byte[] data = new byte[length];

						while (readOffset < length) {
							data[readOffset] = rxBuffer.get();
							readOffset++;
						}
						mpManager.handleMultipartData(this, first, last, token[1], url, data);
						isGetRange = true;
					} else {
						while (true) {
							b = rxBuffer.get();
							if (b == 0x0a)
								break;
						}
					}
				}
			}
		} catch (BufferUnderflowException e) {
			rxBuffer.reset();
		}

	}

	private int handleGzip(HttpResponseImpl response, Buffer rxBuffer) {
		String s = response.getHeader(HttpHeaders.CONTENT_LENGTH);

		if (s == null)
			return response.getStatusCode() == 200 ? DECODE_NOT_READY : 0;

		int length = response.getGzipLength();
		int offset = response.getGzipOffset();
		if (length > DECODE_NOT_READY) {
			try {
				while (offset < length) {
					response.putGzip(rxBuffer.get());
					offset++;
				}
			} catch (BufferUnderflowException e) {
				response.setGzipOffset(offset);
				return DECODE_NOT_READY;
			}
			return 0;
		}
		/* can't handle gzip */
		else
			return 1;
	}

	private int handleGzip(HttpResponseImpl response, List<Byte> chunked) {
		response.putGzip(chunked);
		return 0;
	}

	private void handleDeflate(HttpResponseImpl response, Buffer rxBuffer) {
	}

	private int handleChunked(HttpResponseImpl response, Buffer rxBuffer) {
		/*
		 * return -1: can't get chunked length return 0: size of chunked > chunked size of rxBuffer return 1: flush chunked
		 */

		int retVal;

		while (true) {
			if (response.getChunkedLength() == DECODE_NOT_READY) {
				if (rxBuffer.isEOB())
					return 0;
				rxBuffer.mark();
				rxBuffer.discardReadBytes();

				retVal = getChunkedLength(rxBuffer, response);
				/* failed get chunked length */
				if (retVal == DECODE_NOT_READY)
					return DECODE_NOT_READY;
				/* arrived EOF */
				else if (response.getChunkedLength() == 0)
					break;
				/* succeed get chunked length */
				else {
					retVal = putChunked(rxBuffer, response, response.getChunkedOffset(), response.getChunkedLength());
					if (retVal == DECODE_NOT_READY)
						return 0;
				}
			} else {
				/* already response have chunked length */
				retVal = putChunked(rxBuffer, response, response.getChunkedOffset(), response.getChunkedLength());
				if (retVal == DECODE_NOT_READY)
					return 0;
			}
		}
		return 1;
	}

	private int handleNormal(HttpResponseImpl response, Buffer rxBuffer) {
		/* save response contents until offset is equal to contentLength */
		String s = response.getHeader(HttpHeaders.CONTENT_LENGTH);

		// if status is OK, receive all bytes until session is finished
		// TODO: other error codes(ex. 304) may have contents body
		if (s == null)
			return response.getStatusCode() == 200 ? DECODE_NOT_READY : 0;

		int contentLength = Integer.valueOf(s.replaceAll("\\n", ""));

		/* calculate offset */
		int available = rxBuffer.readableBytes();
		if (available < contentLength)
			return DECODE_NOT_READY;

		byte[] content = new byte[contentLength];
		rxBuffer.gets(content);
		response.getContentBuffer().addLast(content);
		return 0;
	}

	private void decodeContent(HttpResponseImpl response) {
		try {
			Buffer binary = response.getBinary();
			int length = response.getPutLength();
			byte[] b = new byte[length];
			binary.gets(b, 0, length);

			Session session = Session.getDefaultInstance(new Properties());
			InputStream is = new ByteArrayInputStream(b, 0, b.length);
			MimeMessage msg = new MimeMessage(session, is);
			response.setMessage(msg);

			/* set string contents */
			if (msg.getContent() instanceof String) {
				Buffer contentBuffer = response.getContentBuffer();
				int readable = contentBuffer.readableBytes();
				if (readable <= 0)
					return;

				byte[] content = new byte[readable];
				contentBuffer.gets(content);
				response.setContent(content);
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				logger.debug("kraken http decoder: cannot decode content", e);
		}
	}

	private byte[] decompressGzip(List<Byte> gzipContent) throws DataFormatException {
		byte[] gzip = new byte[gzipContent.size()];
		for (int i = 0; i < gzip.length; i++) {
			gzip[i] = gzipContent.get(i);
		}

		try {
			GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(gzip));
			Buffer gzBuffer = new ChainBuffer();

			/* read fixed length(1000 bytes) from gzip contents */
			byte[] newGzip = new byte[1000];
			int readLen = gzis.read(newGzip);
			int sumOfReadLen = 0;

			if (readLen == DECODE_NOT_READY)
				throw new DataFormatException();

			while (readLen != DECODE_NOT_READY) {
				byte[] payload = Arrays.copyOf(newGzip, readLen);
				gzBuffer.addLast(payload);
				newGzip = null;
				newGzip = new byte[1000];
				sumOfReadLen += readLen;
				readLen = gzis.read(newGzip);
			}

			byte[] decompressedGzip = new byte[sumOfReadLen];
			gzBuffer.gets(decompressedGzip);
			return decompressedGzip;
		} catch (BufferUnderflowException e) {
			System.err.println("http decoder: gets error");
		} catch (IOException e) {
			/* case: NOT in GZIP Format */
			return null;
		}
		return null;
	}

	private int getChunkedLength(Buffer rxBuffer, HttpResponseImpl response) {
		try {
			int length = rxBuffer.bytesBefore(new byte[] { 0x0d, 0x0a });
			if (length == 0) {
				response.setChunkedLength(DECODE_NOT_READY);
				return DECODE_NOT_READY;
			}
			String chunkLength = rxBuffer.getString(length).trim();
			int len = Integer.parseInt(chunkLength, 16);
			response.setChunkedLength(len);

			/* skip \r\n */
			rxBuffer.get();
			rxBuffer.get();
		} catch (BufferUnderflowException e) {
			response.setChunkedLength(DECODE_NOT_READY);
			return DECODE_NOT_READY;
		}
		return 0;
	}

	private int putChunked(Buffer rxBuffer, HttpResponseImpl response, int offset, int length) {
		List<Byte> chunked = response.getChunked();
		try {
			while (offset < length) {
				chunked.add(rxBuffer.get());
				offset++;
			}
			rxBuffer.get();
			rxBuffer.get();
			/* when read chunked complete, initialize chunked variables */
			response.setChunkedOffset(0);
			response.setChunkedLength(DECODE_NOT_READY);
		} catch (BufferUnderflowException e) {
			response.setChunkedOffset(offset);
			return DECODE_NOT_READY;
		}
		return 0;
	}

	private void setChunked(HttpResponseImpl response) {
		List<Byte> chunked = response.getChunked();

		byte[] data = new byte[chunked.size()];
		for (int i = 0; i < data.length; i++) {
			data[i] = chunked.get(i);
		}
		response.setChunked(data);
	}

	private void dispatchRequest(HttpRequestImpl request) {
		for (HttpProcessor processor : callbacks) {
			processor.onRequest(request);
		}
	}

	private void dispatchResponse(HttpSession session) {
		session.getResponse().setContent();

		for (HttpProcessor processor : callbacks) {
			processor.onResponse(session.getRequest(), session.getResponse());
		}
	}

	public void dispatchMultipartData(byte[] data, int offset, int length) {
		Buffer bb = new ChainBuffer(Arrays.copyOfRange(data, offset, length));

		for (HttpProcessor processor : callbacks) {
			processor.onMultipartData(bb);
		}
	}
}
