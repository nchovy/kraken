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

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.krakenapps.filter.ActiveFilter;
import org.krakenapps.filter.DefaultMessageSpec;
import org.krakenapps.filter.FilterChain;
import org.krakenapps.filter.Message;
import org.krakenapps.filter.MessageBuilder;
import org.krakenapps.filter.MessageSpec;
import org.krakenapps.filter.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogReceiver extends ActiveFilter implements SyslogServer {
	private static final int QUEUE_SIZE = 20000;

	final Logger logger = LoggerFactory.getLogger(SyslogReceiver.class.getName());

	private static final int DEFAULT_SYSLOG_PORT = 514;
	private DatagramSocket socket;
	private byte[] buffer;
	private FilterChain filterChain;
	private InetAddress address;
	private int serverPort;
	private String charsetName;

	private InternalRunner internalRunner = new InternalRunner();
	private Thread internalRunnerThread = new Thread(internalRunner);
	private LinkedBlockingQueue<Message> packetQueue = new LinkedBlockingQueue<Message>(QUEUE_SIZE);

	private Set<SyslogListener> callbacks;

	public SyslogReceiver() {
		setProperty("port", "514");
		callbacks = Collections.newSetFromMap(new ConcurrentHashMap<SyslogListener, Boolean>());
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return new InetSocketAddress(address, serverPort);
	}

	@Override
	public Charset getCharset() {
		return Charset.forName(charsetName);
	}

	@Override
	public void validateConfiguration() throws ConfigurationException {
		try {
			InetAddress.getByName((String) getProperty("address"));
			Integer.parseInt((String) getProperty("port"));

			charsetName = (String) getProperty("charset");
			if (charsetName != null) {
				Charset.forName(charsetName);
			}
		} catch (UnknownHostException e) {
			throw new ConfigurationException("address", "unknown host address");
		} catch (NumberFormatException e) {
			throw new ConfigurationException("port", "invalid number format");
		} catch (IllegalCharsetNameException e) {
			throw new ConfigurationException("charset", "illegal charset name");
		} catch (UnsupportedCharsetException e) {
			throw new ConfigurationException("charset", "unsupported charset name");
		}
	}

	@Override
	public void open() throws ConfigurationException {
		initializeProperties();

		logger.info("opening syslog receiver, port [{}], address [{}]", serverPort, address);

		try {
			internalRunnerThread.start();
			socket = new DatagramSocket(serverPort, address);
			socket.setSoTimeout(1000);
		} catch (SocketException e) {
			throw new ConfigurationException("socket", e.getMessage());
		}

		buffer = new byte[1024];
	}

	private void initializeProperties() throws ConfigurationException {
		try {
			address = InetAddress.getByName((String) getProperty("address"));
			serverPort = Integer.parseInt((String) getProperty("port"));
			if (serverPort == 0)
				serverPort = DEFAULT_SYSLOG_PORT;

			charsetName = (String) getProperty("charset");
			if (charsetName == null) {
				logger.info("defaulting syslog charset to utf-8.");
				charsetName = "UTF-8";
			}
		} catch (UnknownHostException e) {
			throw new ConfigurationException("address", "unknown host address");
		}
	}

	@Override
	public void close() {
		socket.close();
		internalRunner.stop();
		try {
			internalRunnerThread.join(5000);
		} catch (InterruptedException e) {
			logger.info("SyslogReceiver internal runner is not responding for request for stop.");
		}
		logger.info("syslog receiver closed: {}:{}", address, serverPort);
	}

	private class InternalRunner implements Runnable {
		private boolean isRunning = true;

		public void stop() {
			isRunning = false;
		}

		public void run() {
			while (isRunning) {
				try {
					Message m = packetQueue.take();

					filterChain.process(m);

					// dispatch syslog
					InetSocketAddress addr = new InetSocketAddress((InetAddress) m.get("remote_ip"),
							(Integer) m.get("remote_port"));
					Syslog syslog = new Syslog(new Date(), addr, (Integer) m.get("facility"),
							(Integer) m.get("severity"), (String) m.get("message"));
					for (SyslogListener callback : callbacks) {
						try {
							callback.onReceive(syslog);
						} catch (Exception e) {
							logger.warn("kraken syslog: syslog callback should not throw any exception", e);
						}
					}

					logger.debug((String) m.get("message"));

				} catch (InterruptedException e) {
					if (isRunning)
						logger.info("SyslogReceiver internal runner interrupted, but continue waiting");
					else {
						logger.info("SyslogReceiver internal runner interrupted.");
						break;
					}
				} catch (Exception e) {
					logger.warn("exception occurred in SyslogReceiver internal runner, {}, {}", e.getClass().getName(),
							e.getMessage());
					if (logger.isDebugEnabled())
						logger.debug("exception details", e);
				}
			}
		}
	}

	@Override
	public void run() {
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		try {
			socket.receive(packet);

			MessageBuilder b = new MessageBuilder(getOutputMessageSpec());
			b.set("date", new Date());
			b.set("remote_ip", packet.getAddress());
			b.set("remote_port", new Integer(packet.getPort()));
			b.set("local_ip", address);
			b.set("local_port", new Integer(serverPort));
			String text = new String(packet.getData(), 0, packet.getLength(), charsetName);
			int facility = -1;
			int severity = -1;

			int brace = text.indexOf('>');

			if (text.charAt(0) == '<' && brace > 0 && brace <= 5) {
				int pri = Integer.valueOf(text.substring(1, brace));
				facility = pri / 8;
				severity = pri % 8;
				text = text.substring(brace + 1);
			}
			b.set("facility", facility);
			b.set("severity", severity);

			b.set("message", text);
			Message message = b.build();

			packetQueue.put(message);
		} catch (SocketTimeoutException e) {
		} catch (UnsupportedEncodingException e) {
			logger.warn("unsupported encoding detected", e);
		} catch (Exception e) {
			logger.warn("syslog receive error:", e);
		}
	}

	public int getQueueSize() {
		return packetQueue.size();
	}

	/*
	 * Output satisfies krakenapps.syslog 1.0
	 */
	@Override
	public MessageSpec getOutputMessageSpec() {
		return new DefaultMessageSpec("kraken.syslog", 1, 0);
	}

	@Override
	public void addListener(SyslogListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("syslog listener must not be null");

		callbacks.add(callback);
	}

	@Override
	public void removeListener(SyslogListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("syslog listener must not be null");

		callbacks.remove(callback);

	}

}
