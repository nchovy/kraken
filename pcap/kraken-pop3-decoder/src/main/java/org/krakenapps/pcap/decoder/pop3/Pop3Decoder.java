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
package org.krakenapps.pcap.decoder.pop3;

import java.io.ByteArrayInputStream;
import java.nio.BufferUnderflowException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.krakenapps.pcap.decoder.pop3.impl.Pop3DataImpl;
import org.krakenapps.pcap.decoder.pop3.impl.Pop3Session;
import org.krakenapps.pcap.decoder.pop3.impl.Pop3State;
import org.krakenapps.pcap.decoder.tcp.TcpProcessor;
import org.krakenapps.pcap.decoder.tcp.TcpSessionKey;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.mime.MimeHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindori
 */
public class Pop3Decoder implements TcpProcessor {
	private Logger logger = LoggerFactory.getLogger(Pop3Decoder.class.getName());

	private Set<Pop3Processor> callbacks;
	private Map<TcpSessionKey, Pop3Session> sessionMap;

	public Pop3Decoder() {
		callbacks = new HashSet<Pop3Processor>();
		sessionMap = new HashMap<TcpSessionKey, Pop3Session>();
	}

	public void register(Pop3Processor processor) {
		callbacks.add(processor);
	}

	public void unregister(Pop3Processor processor) {
		callbacks.remove(processor);
	}

	@Override
	public void onEstablish(TcpSessionKey session) {
		if (logger.isDebugEnabled())
			logger.debug("-> POP3 Session Established: " + (int) session.getClientPort() + " -> " + (int) session.getServerPort());
		sessionMap.put(session, new Pop3Session());
	}

	@Override
	public void onFinish(TcpSessionKey session) {
		if (logger.isDebugEnabled())
			logger.debug("-> POP3 Session Closed: \n" + "Client Port: " + (int) session.getClientPort() + "\nServer Port: " + (int) session.getServerPort());
		sessionMap.remove(session);
	}

	@Override
	public void onReset(TcpSessionKey session) {
		Pop3Session pop3Session = sessionMap.get(session);
		if (pop3Session != null) {
			if (logger.isDebugEnabled())
				logger.debug("Deallocate tx, rx buffer and remove pop3 session.");
			pop3Session.clear();
			sessionMap.remove(session);
		}
	}

	@Override
	public void handleTx(TcpSessionKey sessionKey, Buffer data) {
		Pop3Session session = sessionMap.get(sessionKey);
		Buffer txBuffer = session.getTxBuffer();
		txBuffer.addLast(data);

		parseTx(session, txBuffer);
	}

	@Override
	public void handleRx(TcpSessionKey sessionKey, Buffer data) {
		Pop3Session session = sessionMap.get(sessionKey);
		Buffer rxBuffer = session.getRxBuffer();
		rxBuffer.addLast(data);

		parseRx(session, rxBuffer);
	}

	private void parseTx(Pop3Session session, Buffer txBuffer) {
		try {
			int len = txBuffer.bytesBefore(new byte[] { 0x0d, 0x0a });
			if (len == 0) {
				return;
			}

			byte[] t = new byte[len];
			txBuffer.gets(t, 0, t.length);
			/* skip \r\n */
			txBuffer.get();
			txBuffer.get();

			String command = new String(t);
			sendCommand(command);

			handleCommand(command, session, txBuffer);
		} catch (BufferUnderflowException e) {
			txBuffer.reset();
			return;
		}
	}

	private void parseRx(Pop3Session session, Buffer rxBuffer) {
		switch (session.getState()) {
		case NONE:
			try {
				int len = rxBuffer.bytesBefore(new byte[] { 0x0d, 0x0a });
				if (len == 0) {
					return;
				}

				byte[] t = new byte[len];
				rxBuffer.gets(t, 0, t.length);
				/* skip \r\n */
				rxBuffer.get();
				rxBuffer.get();

				String response = new String(t);
				sendResponse(response);
			} catch (BufferUnderflowException e) {
				rxBuffer.reset();
				return;
			}
			break;

		case FIND_UIDL:
		case FIND_LIST:
			try {
				int len = rxBuffer.bytesBefore(new byte[] { 0x0d, 0x0a, 0x2e, 0x0d, 0x0a });
				if (len == 0) {
					return;
				}

				byte[] t = new byte[len + 5];
				rxBuffer.gets(t, 0, t.length);
			} catch (BufferUnderflowException e) {
				rxBuffer.reset();
				return;
			}
			break;

		case FIND_TOP:
			try {
				while (true) {
					rxBuffer.get();
				}
			} catch (BufferUnderflowException e) {
				return;
			}

		case FIND_RETR:
			if (!session.isSkipRETRMessage()) {
				/* skip response message */
				try {
					int len = rxBuffer.bytesBefore(new byte[] { 0x0d, 0x0a });
					if (len == 0) {
						return;
					}

					byte[] t = new byte[len + 2];
					rxBuffer.gets(t, 0, t.length);
					session.setSkipRETRMessage(true);
				} catch (BufferUnderflowException e) {
					rxBuffer.reset();
					return;
				}
			}

			else {
				if (!session.isRemarkStart()) {
					/* record start point of e-mail */
					session.setRemarkStart(true);
				}
				int length = rxBuffer.bytesBefore(new byte[] { 0x0d, 0x0a, 0x2e, 0x0d, 0x0a });
				if (length == 0) {
					return;
				}
				byte[] emailData = new byte[length];
				rxBuffer.gets(emailData, 0, length);

				MimeMessage msg = createMimeMessage(emailData);
				MimeHeader header = new MimeHeader();
				Charset headerCharset = header.getHeaderCharset(msg);
				header.decodeHeader(headerCharset, emailData);

				Pop3DataImpl pop3Data = new Pop3DataImpl(msg);
				getMessage(header, pop3Data);

				/* skip 'CRLF.CRLF' */
				byte[] t = new byte[5];
				rxBuffer.gets(t, 0, t.length);

				/* initialize e-mail variables */
				session.setRemarkStart(false);
				session.setSkipRETRMessage(false);

				/* deallocate and reallocate */
				session.clear();
				session.setState(Pop3State.NONE);
			}
			break;
		case FIND_DELE:
			/* skip '+OK' */
			byte[] msg = new byte[6];
			rxBuffer.gets(msg);
			break;
		}
	}

	private void handleCommand(String command, Pop3Session session, Buffer txBuffer) {
		if (command.equalsIgnoreCase("UIDL")) {
			session.setState(Pop3State.FIND_UIDL);
		} else if (command.equalsIgnoreCase("LIST")) {
			session.setState(Pop3State.FIND_LIST);
		} else if (command.length() > 4 && command.substring(0, 3).equalsIgnoreCase("TOP")) {
			session.setState(Pop3State.FIND_TOP);
		} else if (command.length() > 5 && command.substring(0, 4).equalsIgnoreCase("RETR")) {
			session.setState(Pop3State.FIND_RETR);
		} else if (command.length() > 5 && command.substring(0, 4).equalsIgnoreCase("DELE")) {
			session.setState(Pop3State.FIND_DELE);
		} else if (session.getState().compareTo(Pop3State.FIND_TOP) == 0) {
			session.setState(Pop3State.NONE);
		}
	}

	private MimeMessage createMimeMessage(byte[] data) {
		Session mailSession = Session.getDefaultInstance(new Properties());
		InputStream is = new ByteArrayInputStream(data, 0, data.length);

		try {
			return new MimeMessage(mailSession, is);
		} catch (MessagingException e) {
			logger.error("pop3 decoder: mime parse error" + e);
		}
		return null;
	}

	private void getMessage(MimeHeader header, Pop3DataImpl pop3Data) {
		for (Pop3Processor p : callbacks)
			p.onReceive(header, pop3Data);
	}

	private void sendCommand(String command) {
		for (Pop3Processor p : callbacks)
			p.onCommand(command);
	}

	private void sendResponse(String response) {
		for (Pop3Processor p : callbacks)
			p.onResponse(response);
	}
}