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
package org.krakenapps.pcap.decoder.smtp;

import java.io.ByteArrayInputStream;
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

import org.krakenapps.mime.MimeHeader;
import org.krakenapps.pcap.decoder.smtp.impl.SmtpDataImpl;
import org.krakenapps.pcap.decoder.smtp.impl.SmtpSession;
import org.krakenapps.pcap.decoder.tcp.TcpProcessor;
import org.krakenapps.pcap.decoder.tcp.TcpSessionKey;
import org.krakenapps.pcap.util.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindori
 */
public class SmtpDecoder implements TcpProcessor {
	private Logger logger = LoggerFactory.getLogger(SmtpDecoder.class.getName());

	private Set<SmtpProcessor> callbacks;
	private Map<TcpSessionKey, SmtpSession> sessionMap;

	public SmtpDecoder() {
		callbacks = new HashSet<SmtpProcessor>();
		sessionMap = new HashMap<TcpSessionKey, SmtpSession>();
	}

	public void register(SmtpProcessor processor) {
		callbacks.add(processor);
	}

	public void unregister(SmtpProcessor processor) {
		callbacks.remove(processor);
	}

	@Override
	public void handleTx(TcpSessionKey sessionKey, Buffer data) {
		SmtpSession session = sessionMap.get(sessionKey);
		Buffer txBuffer = session.getTxBuffer();
		txBuffer.addLast(data);

		handleTx(session, txBuffer);
	}

	@Override
	public void handleRx(TcpSessionKey sessionKey, Buffer data) {
		SmtpSession session = sessionMap.get(sessionKey);
		Buffer rxBuffer = session.getRxBuffer();
		rxBuffer.addLast(data);

		handleRx(session, rxBuffer);
	}

	@Override
	public void onEstablish(TcpSessionKey session) {
		if (logger.isDebugEnabled())
			logger.debug("-> SMTP Session Established: " + (int) session.getClientPort() + " -> "
					+ (int) session.getServerPort());
		sessionMap.put(session, new SmtpSession());
	}

	@Override
	public void onFinish(TcpSessionKey session) {
		if (logger.isDebugEnabled())
			logger.debug("-> SMTP Session Closed: \n" + "Client Port: "
					+ (int) session.getClientPort() + "\nServer Port: "
					+ (int) session.getServerPort());
		sessionMap.remove(session);
	}

	@Override
	public void onReset(TcpSessionKey session) {
		SmtpSession smtpSession = sessionMap.get(session);
		if (smtpSession != null) {
			if (logger.isDebugEnabled())
				logger.debug("Deallocate tx, rx buffer and remove smtp session.");
			smtpSession.clear();
			sessionMap.remove(session);
		}
	}

	private void handleTx(SmtpSession session, Buffer buf) {
		if (session.isDataMode()) {
			handleClientData(session, buf);
		} else {
			handleClientCommand(session, buf);
			session.resetTx();
		}

	}

	private void handleRx(SmtpSession session, Buffer buf) {
		handleReply(session, buf);
		session.resetRx(); 
	}

	private void handleClientData(SmtpSession session, Buffer txBuffer) {
		// store until find \r\n.\r\n
		int length = txBuffer.bytesBefore(new byte[] { 0x0d, 0x0a, 0x2e, 0x0d, 0x0a });
		if (length == 0) {
			return;
		}

		session.setDataMode(false);
		byte[] emailData = new byte[length];
		txBuffer.gets(emailData, 0, length);

		MimeMessage msg = createMimeMessage(emailData);
		MimeHeader header = new MimeHeader();
		Charset headerCharset = header.getHeaderCharset(msg);
		header.decodeHeader(headerCharset, emailData);

		SmtpDataImpl smtpData = new SmtpDataImpl(msg);
		getMessage(header, smtpData);
		
		session.reset();
	}

	private void handleClientCommand(SmtpSession session, Buffer txBuffer) {
		while(true) { 
			int length = txBuffer.bytesBefore(new byte[] { 0x0d, 0x0a });
			if (length == 0) {
				return;
			}
			
			byte[] b = new byte[length];
			txBuffer.gets(b, 0, length);
			/* skip \r\n */
			txBuffer.get();
			txBuffer.get();
			
			String command = new String(b);
			if(command.equals("DATA")) {
				session.setDataMode(true);
				dispatchCommand(command, "");
				break;
			}
			else if(command.matches("\\w{4} .+")) {
				String parameter = command.substring(5);
				command = command.substring(0, 4);
				dispatchCommand(command, parameter);
			}
			else {
				/* don't have parameter(ex. QUIT) */
				dispatchCommand(command, "");
			}
		}
	}

	private void handleReply(SmtpSession session, Buffer rxBuffer) {
		while(true) { 
			int length = rxBuffer.bytesBefore(new byte[] { 0x0d, 0x0a });
			if (length == 0) {
				return;
			}
			
			byte[] b = new byte[length];
			rxBuffer.gets(b, 0, length);
			/* skip \r\n */
			rxBuffer.get();
			rxBuffer.get();
			
			String reply = new String(b);
			if (reply.matches("\\d{3}.+")) {
				int replyCode = Integer.parseInt(reply.substring(0, 3));
				dispatchReply(replyCode, reply.substring(4));
			}
		}
	}

	private MimeMessage createMimeMessage(byte[] data) {
		Session mailSession = Session.getDefaultInstance(new Properties());
		InputStream is = new ByteArrayInputStream(data, 0, data.length);

		try {
			return new MimeMessage(mailSession, is);
		} catch (MessagingException e) {
			logger.error("smtp decoder: mime parse error" + e);
		}
		return null;
	}

	private void getMessage(MimeHeader header, SmtpDataImpl smtpData) {
		for (SmtpProcessor processor : callbacks) {
			processor.onSend(header, smtpData);
		}
	}

	private void dispatchCommand(String command, String parameter) {
		for (SmtpProcessor processor : callbacks) {
			processor.onCommand(command, parameter);
		}
	}

	private void dispatchReply(int replyCode, String replyMessage) {
		for (SmtpProcessor processor : callbacks) {
			processor.onReply(replyCode, replyMessage);
		}
	}
}