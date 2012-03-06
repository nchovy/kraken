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
package org.krakenapps.syslog;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.krakenapps.filter.ActiveFilter;
import org.krakenapps.filter.Message;
import org.krakenapps.filter.MessageSpec;
import org.krakenapps.filter.DefaultMessageSpec;
import org.krakenapps.filter.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogSender extends ActiveFilter {
	private DatagramSocket socket;
	private BlockingQueue<Message> messageQueue;
	private final Logger logger = LoggerFactory.getLogger(SyslogSender.class.getName());
	private int facility = SyslogFacility.Local0.getCode();
	private Charset encoding;

	@Override
	public void open() throws ConfigurationException {
		try {
			InetAddress address = getServerAddress();
			int port = getPort();

			socket = new DatagramSocket();
			socket.connect(address, port);

			messageQueue = new LinkedBlockingQueue<Message>();
			facility = getFacility();
			encoding = getEncoding();

			logger.info("opening syslog sender: {}", socket.getRemoteSocketAddress());
		} catch (UnknownHostException e) {
			throw new ConfigurationException("address", "unknown host address");
		} catch (SocketException e) {
			throw new ConfigurationException("socket", e.getMessage());
		}
	}

	@Override
	public void close() {
		logger.info("syslog sender closed: {}", socket.getRemoteSocketAddress());

		messageQueue.clear();
		messageQueue = null;

		socket.close();
		socket = null;
	}

	@Override
	public void run() throws InterruptedException {
		Message message = messageQueue.take();
		String msg = (String) message.get("message");
		if (msg == null)
			return;

		if (message.get("severity") != null) {
			int severity = (Integer) message.get("severity");
			int pri = facility * 8 + severity;
			msg = "<" + pri + ">" + msg;
		}

		ByteBuffer byteBuffer = encoding.encode(msg);
		byte[] buffer = new byte[byteBuffer.remaining()];
		byteBuffer.get(buffer);

		try {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.send(packet);

			if (logger.isTraceEnabled())
				logger.trace("to [{}], {}", socket.getRemoteSocketAddress(), msg);
		} catch (IOException e) {
			logger.warn("syslog sender error:", e);
		}
	}

	@Override
	public MessageSpec[] getInputMessageSpecs() {
		return new MessageSpec[] { new DefaultMessageSpec("kraken.syslog", 1, 0),
				new DefaultMessageSpec("kraken.syslog.sender", 1, 0),
				new DefaultMessageSpec("kraken.syslog.sender", 1, 1) };
	}

	@Override
	public void process(Message message) {
		messageQueue.add(message);
	}

	private InetAddress getServerAddress() throws UnknownHostException {
		return InetAddress.getByName((String) getProperty("address"));
	}

	private int getPort() {
		return Integer.parseInt((String) getProperty("port"));
	}

	private int getFacility() {
		String f = (String) getProperty("facility");
		if (f != null) {
			return Integer.parseInt(f);
		}

		return facility;
	}

	private Charset getEncoding() {
		String encoding = (String) getProperty("encoding");
		if (encoding == null)
			return Charset.forName("utf-8");

		return Charset.forName(encoding);
	}

}
