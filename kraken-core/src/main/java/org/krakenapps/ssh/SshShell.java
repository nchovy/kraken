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
package org.krakenapps.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.sshd.common.SshException;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.krakenapps.ansicode.AnsiEscapeCode;
import org.krakenapps.api.ScriptOutputStream;
import org.krakenapps.api.TelnetCommand;
import org.krakenapps.console.ConsoleInputStream;
import org.krakenapps.console.QuitHandler;
import org.krakenapps.console.ShellSession;
import org.krakenapps.console.TelnetStateMachine;
import org.krakenapps.main.Kraken;
import org.krakenapps.script.ScriptContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshShell implements Command, Runnable, QuitHandler {
	private final Logger logger = LoggerFactory.getLogger(SshShell.class.getName());
	private ShellSession session;
	private InputStream in;
	private ExitCallback callback;
	private Thread thread;
	private TelnetStateMachine tsm;
	private ScriptContextImpl context;

	public SshShell() {
		context = new ScriptContextImpl(Kraken.getContext(), this);
		session = new ShellSession(context);
		tsm = new TelnetStateMachine(new MessageReceiver(session), context);
	}

	@Override
	public void onQuit() {
		thread.interrupt();
	}

	@Override
	public void setInputStream(InputStream in) {
		this.in = in;
		session.getScriptContext().setInputStream(new ConsoleInputStream(session.getScriptContext()));
	}

	@Override
	public void setOutputStream(OutputStream out) {
		session.getScriptContext().setOutputStream(new SshOutputStream(out));
	}

	@Override
	public void setErrorStream(OutputStream err) {
	}

	@Override
	public void setExitCallback(ExitCallback callback) {
		this.callback = callback;
	}

	@Override
	public void start(Environment env) throws IOException {
		int width = Integer.parseInt(env.getEnv().get(Environment.ENV_COLUMNS));
		int height = Integer.parseInt(env.getEnv().get(Environment.ENV_LINES));
		context.setWindowSize(width, height);

		String username = env.getEnv().get(Environment.ENV_USER);
		session.setPrincipal(username);

		thread = new Thread(this, "SshShell");
		thread.start();
	}

	@Override
	public void destroy() {
		thread.interrupt();
	}

	@Override
	public void run() {
		session.printBanner();
		context.printPrompt();
		try {
			for (;;) {
				byte b = (byte) in.read();
				tsm.feed(b);
			}
		} catch (Exception e) {
			if (!(e instanceof InterruptedException))
				e.printStackTrace();
		} finally {
			callback.onExit(0);
		}
	}

	static class MessageReceiver implements ProtocolDecoderOutput {
		private ShellSession session;

		public MessageReceiver(ShellSession session) {
			this.session = session;
		}

		@Override
		public void flush(NextFilter nextFilter, IoSession session) {
		}

		@Override
		public void write(Object message) {
			try {
				session.handleMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	class SshOutputStream implements ScriptOutputStream {
		private OutputStream out;

		public SshOutputStream(OutputStream out) {
			this.out = out;
		}

		@Override
		public ScriptOutputStream print(AnsiEscapeCode code) {
			try {
				out.write(code.toByteArray());
				out.flush();
			} catch (Exception e) {
				logger.error("kraken core: print error", e);
			}
			return this;
		}

		@Override
		public ScriptOutputStream printf(String format, Object... args) {
			try {
				String text = String.format(format, args);
				text = text.replaceAll("\n", "\r\n");
				byte[] b = text.getBytes("utf-8");

				out.write(b);
				out.flush();
			} catch (UnsupportedEncodingException e) {
				logger.error("kraken core: printf error", e);
			} catch (IOException e) {
				logger.error("kraken core: printf error", e);
			}

			return this;
		}

		@Override
		public ScriptOutputStream print(String value) {
			try {
				byte[] b = value.getBytes("utf-8");
				out.write(b);
				out.flush();
			} catch (IOException e) {
				logger.error("kraken core: print error", e);
				if (e instanceof SshException && e.getMessage().equals("Already closed")) {
					throw new IllegalStateException("SSH: Already Closed");
				}
			}
			return this;
		}

		@Override
		public ScriptOutputStream println(String value) {
			print(value);
			print("\r\n");
			return this;
		}

		@Override
		public ScriptOutputStream print(TelnetCommand command) {
			try {
				out.write(TelnetCommand.InterpretAsControl);
				out.write(command.toByteArray());
			} catch (IOException e) {
				logger.error("kraken core: print error", e);
			}
			return this;
		}

	}
}
