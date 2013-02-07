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
package org.krakenapps.pcap.decoder.ftp;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.decoder.tcp.TcpProtocolMapper;
import org.krakenapps.pcap.decoder.tcp.TcpProcessor;
import org.krakenapps.pcap.decoder.tcp.TcpSessionKey;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.BufferInputStream;
import org.krakenapps.pcap.util.ChainBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindori
 */
public class FtpDecoder implements TcpProcessor {
	private Logger logger = LoggerFactory.getLogger(FtpDecoder.class.getName());
	private Set<FtpProcessor> callbacks;
	private Map<TcpSessionKey, FtpSession> sessionMap;
	private final TcpProtocolMapper mapper;

	/* multi-session variables */
	private FtpDataSession dataSession;
	private TcpSessionKey key;
	private boolean isDownload = false;
	private String fileName = "";

	/* list variables */
	private boolean isViewList = false;
	private Buffer list;

	public FtpDecoder(TcpProtocolMapper mapper) {
		callbacks = new HashSet<FtpProcessor>();
		sessionMap = new HashMap<TcpSessionKey, FtpSession>();
		this.mapper = mapper;
	}

	public void register(FtpProcessor processor) {
		callbacks.add(processor);
	}

	public void unregister(FtpProcessor processor) {
		callbacks.remove(processor);
	}

	@Override
	public void handleTx(TcpSessionKey sessionKey, Buffer data) {
		FtpSession session = sessionMap.get(sessionKey);
		if (isDownload && !(sessionKey.equals(key)))
			dataSession.putData(data);
		else
			handleTxBuffer(sessionKey, session, data);
	}

	@Override
	public void handleRx(TcpSessionKey sessionKey, Buffer data) {
		FtpSession session = sessionMap.get(sessionKey);
		if (isDownload && !(sessionKey.equals(key)))
			dataSession.putData(data);
		else
			handleRxBuffer(sessionKey, session, data);
	}

	@Override
	public void onEstablish(TcpSessionKey session) {
		if (logger.isDebugEnabled())
			logger.debug("-> ftp Session Established: " + (int) session.getClientPort() + " -> "
					+ (int) session.getServerPort());
		sessionMap.put(session, new FtpSession());
	}

	@Override
	public void onFinish(TcpSessionKey session) {
		if (logger.isDebugEnabled())
			logger.debug("-> ftp Session Closed: \n" + "Client Port: " + (int) session.getClientPort()
					+ "\nServer Port: " + (int) session.getServerPort());
		sessionMap.remove(session);
	}

	@Override
	public void onReset(TcpSessionKey session) {
		FtpSession ftpSession = sessionMap.get(session);
		if (ftpSession != null) {
			if (logger.isDebugEnabled())
				logger.debug("Deallocate tx, rx buffer and remove ftp session.");
			ftpSession.clear();
			sessionMap.remove(session);
		}
	}

	private void handleTxBuffer(TcpSessionKey key, FtpSession session, Buffer data) {
		Buffer txBuffer = session.getTxBuffer();
		txBuffer.addLast(data);
		handleCommandSession(key, session, txBuffer);
	}

	private void handleRxBuffer(TcpSessionKey key, FtpSession session, Buffer data) {
		Buffer rxBuffer = session.getRxBuffer();
		rxBuffer.addLast(data);
		handleCommandSession(key, session, rxBuffer);
	}

	private void handleCommandSession(TcpSessionKey key, FtpSession session, Buffer buffer) {
		while (true) {
			if (buffer.isEOB()) {
				break;
			}

			int length = buffer.bytesBefore(new byte[] { 0x0d, 0x0a });
			if (length == 0)
				return;

			byte[] codes = new byte[4];
			buffer.mark();
			buffer.gets(codes, 0, 4);
			buffer.reset();

			String code = new String(codes);
			if (code.matches("\\d{3} ")) {
				byte[] reply = new byte[length];
				buffer.gets(reply, 0, length);
				/* skip \r\n */
				buffer.get();
				buffer.get();
				dispatchReply(new String(reply));

				if (code.equals("227 ")) {
					/* passive mode */
					String replyStr = new String(reply);
					String[] token = replyStr.split(" ");

					InetSocketAddress sockAddr = new InetSocketAddress(key.getServerIp(), getPort(token));
					mapper.register(sockAddr, Protocol.FTP);
				}

				else if (code.equals("226 ") || code.equals("250 ")) {
					/* get attached file */
					if (fileName != "") {
						Buffer data = dataSession.getData();
						BufferInputStream is = new BufferInputStream(data);
						dispatchFile(is, fileName);
						initMultiSession();
					}
					/* view directory list */
					else if (isViewList) {
						int remain = list.readableBytes();

						if (remain <= 0) {
							/* get directory list. But, list is empty. */
							list = null;
							isViewList = false;
						} else {
							byte[] b = new byte[remain];
							list.gets(b, 0, remain);
							dispatchList(b);

							list = null;
							isViewList = false;
						}
					}
				}
			}

			else {
				byte[] command = new byte[length];
				buffer.gets(command, 0, length);
				/* skip \r\n */
				buffer.get();
				buffer.get();
				String commandStr = new String(command);

				if (isViewList) {
					list.addLast(command);
					list.addLast(new byte[] { 0x0d, 0x0a });
				}

				else if (code.equalsIgnoreCase("LIST") || code.equalsIgnoreCase("NLST")) {
					fileName = "";
					list = new ChainBuffer();
					isViewList = true;
					dispatchCommand(commandStr);
				}

				else if (code.equalsIgnoreCase("STOR") || code.equalsIgnoreCase("RETR")) {
					dataSession = new FtpDataSession();

					fileName = commandStr.split(" ")[1].replaceAll("\r\n", "");
					isDownload = true;
					this.key = key;
					dispatchCommand(commandStr);
				}

				else if (code.equalsIgnoreCase("PORT")) {
					/* active mode - REGISTER PORT */
					String[] token = commandStr.split(" ");

					InetSocketAddress sockAddr = new InetSocketAddress(key.getClientIp(), getPort(token));
					mapper.register(sockAddr, Protocol.FTP);

					dispatchCommand(commandStr);
				} else {
					dispatchCommand(commandStr);
				}
			}
		}
		session.reset();
	}

	private int getPort(String[] token) {
		String[] portCommand = token[token.length - 1].replaceAll("[()]\\.", "").split(",");
		int port = (Integer.parseInt(portCommand[4]) * 256) + Integer.parseInt(portCommand[5].replaceAll("\r\n", ""));
		return port;
	}

	private void initMultiSession() {
		dataSession = null;
		key = null;
		isDownload = false;
		fileName = "";
	}

	private void dispatchCommand(String command) {
		for (FtpProcessor processor : callbacks) {
			processor.onCommand(command);
		}
	}

	private void dispatchReply(String reply) {
		for (FtpProcessor processor : callbacks) {
			processor.onReply(reply);
		}
	}

	private void dispatchList(byte[] list) {
		for (FtpProcessor processor : callbacks) {
			processor.viewList(list);
		}
	}

	private void dispatchFile(InputStream is, String fileName) {
		for (FtpProcessor processor : callbacks) {
			processor.onExtractFile(is, fileName);
		}
	}
}
