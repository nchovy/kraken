/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.honey.sshd.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import org.krakenapps.honey.sshd.HoneyFileSystem;
import org.krakenapps.honey.sshd.HoneySshService;
import org.krakenapps.honey.sshd.HoneySshSession;
import org.krakenapps.termio.TerminalDecoder;
import org.krakenapps.termio.TerminalEventListener;
import org.krakenapps.termio.TerminalInputStream;
import org.krakenapps.termio.TerminalOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoneySshSessionImpl implements Command, Runnable, HoneySshSession, SessionAware {
	private final Logger logger = LoggerFactory.getLogger(HoneySshSessionImpl.class.getName());

	private HoneySshService sshd;
	private HoneyFileSystem fs;

	private Environment env;
	private ServerSession session;
	private InputStream in;
	private OutputStream out;
	private OutputStream err;

	private TerminalInputStream tin;
	private TerminalOutputStream tout;

	private ExitCallback callback;
	private TerminalDecoder decoder;
	private Map<String, String> environmentVariables;

	private Thread t;
	private CopyOnWriteArraySet<TerminalEventListener> listeners = new CopyOnWriteArraySet<TerminalEventListener>();

	public HoneySshSessionImpl(HoneySshService sshd) {
		this.sshd = sshd;
		this.fs = new HoneyFileSystemImpl(sshd.getRootPath());
		this.decoder = new TerminalDecoder(this);
		this.environmentVariables = new HashMap<String, String>();
	}

	@Override
	public void setSession(ServerSession session) {
		this.session = session;
	}

	@Override
	public void setInputStream(InputStream in) {
		this.in = in;
		this.tin = new TerminalInputStream();
	}

	@Override
	public void setOutputStream(OutputStream out) {
		this.out = out;
		this.tout = new TerminalOutputStream(out);
	}

	@Override
	public void setErrorStream(OutputStream err) {
		this.err = err;
	}

	@Override
	public void setExitCallback(ExitCallback callback) {
		this.callback = callback;
	}

	@Override
	public void start(Environment env) throws IOException {
		this.env = env;
		t = new Thread(this, "Honey SSH Shell");
		t.start();

		for (TerminalEventListener listener : listeners)
			listener.onConnected(this);
	}

	@Override
	public void destroy() {
		t.interrupt();
	}

	@Override
	public void run() {
		byte[] b = new byte[512];
		try {
			while (true) {
				int length = in.read(b);
				for (int i = 0; i < length; i++)
					decoder.feed(b[i]);
			}
		} catch (Exception e) {
			if (e instanceof InterruptedException)
				logger.info("kraken honey sshd: interrupted");
			else
				logger.error("kraken honey sshd: terminating shell", e);
		} finally {

		}
	}

	@Override
	public String getUsername() {
		return env.getEnv().get(Environment.ENV_USER);
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return (InetSocketAddress) session.getIoSession().getRemoteAddress();
	}

	@Override
	public String getEnvironmentVariable(String key) {
		return environmentVariables.get(key);
	}

	@Override
	public void setEnvironmentVariable(String key, String value) {
		environmentVariables.put(key, value);
	}

	@Override
	public TerminalInputStream getInputStream() {
		return tin;
	}

	@Override
	public void setInputStream(TerminalInputStream in) {
		this.in = in;
	}

	@Override
	public TerminalOutputStream getOutputStream() {
		return tout;
	}

	@Override
	public void setOutputStream(TerminalOutputStream out) {
		this.out = out;
	}

	@Override
	public void close() {
		callback.onExit(0);
	}

	@Override
	public Set<TerminalEventListener> getListeners() {
		return listeners;
	}

	@Override
	public void addListener(TerminalEventListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(TerminalEventListener listener) {
		listeners.remove(listener);
	}

	@Override
	public HoneySshService getHoneySshService() {
		return sshd;
	}

	@Override
	public HoneyFileSystem getHoneyFileSystem() {
		return fs;
	}

}
