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
package org.krakenapps.console;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.krakenapps.script.ScriptContextImpl;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelnetHandler extends IoHandlerAdapter {
	final Logger logger = LoggerFactory
			.getLogger(TelnetHandler.class.getName());
	private BundleContext bc;

	public TelnetHandler(BundleContext bc) {
		this.bc = bc;
	}

	public void sessionOpened(IoSession session) throws Exception {
		ScriptContextImpl scriptContext = newScriptContext(session);
		ShellSession shellSession = new ShellSession(scriptContext);
		session.setAttribute("session", shellSession);

		logger.info("telnet shell opened from: " + session.getRemoteAddress());
		session.write(new TelnetOptionControl(TelnetOptionMessageType.WILL,
				TelnetOptionCode.Echo));
		session.write(new TelnetOptionControl(TelnetOptionMessageType.WILL,
				TelnetOptionCode.SuppressGoAhead));
		session.write("login as: ");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		logger.info("console closed from: " + session.getRemoteAddress());
	}

	private ShellSession getShellSession(IoSession session) {
		ShellSession shellSession = (ShellSession) session
				.getAttribute("session");
		return shellSession;
	}

	private ScriptContextImpl newScriptContext(IoSession session) {
		ScriptContextImpl context = new ScriptContextImpl(bc);
		context.setInputStream(new ConsoleInputStream(context));
		context.setOutputStream(new ConsoleOutputStream(session));
		return context;
	}

	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		if (logger.isTraceEnabled())
			logger.trace("exception detail", cause);
	}

	public void messageReceived(IoSession session, Object message)
			throws Exception {
		try {
			ShellSession shellSession = getShellSession(session);
			shellSession.handleMessage(message);
		} catch (RuntimeException e) {
			if (e.getMessage().equals("quit"))
				session.close(false);
			else
				throw e;
		}
	}
}
