package org.krakenapps.honey.sshd.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.krakenapps.termio.TerminalDecoder;
import org.krakenapps.termio.TerminalEventListener;
import org.krakenapps.termio.TerminalSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoneySshSessionImpl implements Command, Runnable, TerminalSession {
	private final Logger logger = LoggerFactory.getLogger(HoneySshSessionImpl.class.getName());

	private Environment env;
	private InputStream in;
	private OutputStream out;
	private OutputStream err;
	private ExitCallback callback;
	private TerminalDecoder decoder;
	private Map<String, String> environmentVariables;

	private Thread t;
	private CopyOnWriteArraySet<TerminalEventListener> listeners = new CopyOnWriteArraySet<TerminalEventListener>();

	public HoneySshSessionImpl() {
		decoder = new TerminalDecoder(this);
		environmentVariables = new HashMap<String, String>();
	}

	@Override
	public void setInputStream(InputStream in) {
		this.in = in;
	}

	@Override
	public void setOutputStream(OutputStream out) {
		this.out = out;
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
	public String getEnvironmentVariable(String key) {
		return environmentVariables.get(key);
	}

	@Override
	public void setEnvironmentVariable(String key, String value) {
		environmentVariables.put(key, value);
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		out.write(b);
	}

	@Override
	public void flush() throws IOException {
		out.flush();
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

}
