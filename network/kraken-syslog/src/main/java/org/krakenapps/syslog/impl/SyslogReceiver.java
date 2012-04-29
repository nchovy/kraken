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
package org.krakenapps.syslog.impl;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.krakenapps.syslog.Syslog;
import org.krakenapps.syslog.SyslogListener;
import org.krakenapps.syslog.SyslogProfile;
import org.krakenapps.syslog.SyslogServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogReceiver implements SyslogServer, Runnable {
	final Logger logger = LoggerFactory.getLogger(SyslogReceiver.class.getName());

	private SyslogProfile profile;
	private DatagramSocket socket;
	private byte[] buffer;
	private Charset charset;

	private PushRunner internalRunner = new PushRunner();
	private Thread thread;
	private Thread pushRunnerThread;
	private LinkedBlockingQueue<Syslog> packetQueue;

	private Set<SyslogListener> callbacks;
	private Date bootTime = new Date();
	private AtomicLong counter = new AtomicLong();

	private volatile boolean doStop = false;
	private volatile boolean doStopPush = false;

	public SyslogReceiver(SyslogProfile profile) {
		this.profile = profile;
		this.charset = Charset.forName(profile.getCharset());
		this.packetQueue = new LinkedBlockingQueue<Syslog>(profile.getQueueSize());
		this.callbacks = Collections.newSetFromMap(new ConcurrentHashMap<SyslogListener, Boolean>());
		this.thread = new Thread(this, "Syslog " + profile.getListenAddress());
		this.pushRunnerThread = new Thread(internalRunner, "Syslog Push " + profile.getListenAddress());
	}

	@Override
	public InetSocketAddress getListenAddress() {
		return new InetSocketAddress(profile.getAddress(), profile.getPort());
	}

	@Override
	public Charset getCharset() {
		return Charset.forName(profile.getCharset());
	}

	public void open() throws SocketException {
		if (socket != null)
			throw new IllegalStateException("already opened");

		logger.info("kraken syslog: opening syslog server [{}]", profile);

		socket = new DatagramSocket(getListenAddress());
		socket.setSoTimeout(500);
		bootTime = new Date();
		buffer = new byte[1024];

		doStop = false;
		doStopPush = false;
		pushRunnerThread.start();
		thread.start();
	}

	public void close() {
		if (socket == null)
			return;

		doStop = true;
		doStopPush = true;
		socket.close();

		try {
			pushRunnerThread.interrupt();
			pushRunnerThread.join(2500);
			thread.join(2500);
		} catch (InterruptedException e) {
			logger.warn("kraken syslog: internal runner didn't respond for stop request");
		}
		logger.info("kraken syslog: closed server [{}]", profile);
	}

	private class PushRunner implements Runnable {
		public void run() {
			try {
				while (!doStopPush) {
					try {
						Syslog syslog = packetQueue.take();
						counter.incrementAndGet();

						// dispatch syslog
						for (SyslogListener callback : callbacks) {
							try {
								callback.onReceive(syslog);
							} catch (Exception e) {
								logger.warn("kraken syslog: syslog callback should not throw any exception", e);
							}
						}
					} catch (InterruptedException e) {
						if (doStop) {
							logger.info("kraken syslog: internal runner interrupted.");
							break;
						} else
							logger.info("kraken syslog: internal runner interrupted, but continue waiting");
					} catch (Exception e) {
						logger.warn("kraken syslog: internal runner error, {}, {}", e.getClass().getName(), e.getMessage());
						if (logger.isDebugEnabled())
							logger.debug("kraken syslog: exception details", e);
					}
				}
			} finally {
				logger.info("kraken syslog: server [{}] stopped", profile.getName());
				doStopPush = false;
			}
		}
	}

	@Override
	public void run() {
		try {
			while (!doStop) {
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				try {
					socket.receive(packet);
					String text = new String(packet.getData(), 0, packet.getLength(), charset);
					int facility = -1;
					int severity = -1;

					int brace = text.indexOf('>');

					if (text.charAt(0) == '<' && brace > 0 && brace <= 5) {
						int pri = Integer.valueOf(text.substring(1, brace));
						facility = pri / 8;
						severity = pri % 8;
						text = text.substring(brace + 1);
					}

					InetSocketAddress remote = new InetSocketAddress(packet.getAddress(), packet.getPort());
					Syslog syslog = new Syslog(new Date(), remote, facility, severity, text);
					syslog.setLocalAddress((InetSocketAddress) packet.getSocketAddress());
					packetQueue.put(syslog);
				} catch (SocketTimeoutException e) {
				} catch (UnsupportedEncodingException e) {
					logger.warn("kraken syslog: unsupported encoding detected", e);
				} catch (Throwable t) {
					logger.warn("kraken syslog: receive error", t);
				}
			}
		} finally {
			doStop = false;
		}
	}

	public int getQueueSize() {
		return packetQueue.size();
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

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String since = dateFormat.format(bootTime);
		int pending = packetQueue.size();

		return profile.toString() + ", since=" + since + ", received=" + counter.get() + ", pending=" + pending;
	}

}
