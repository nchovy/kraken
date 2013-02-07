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
package org.krakenapps.test;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.logging.Logger;
import org.krakenapps.console.ConsoleInputStream;
import org.krakenapps.console.TelnetOptionCode;
import org.krakenapps.console.TelnetOptionControl;
import org.krakenapps.console.TelnetOptionMessageType;
import org.krakenapps.test.ConsoleOutputStream;
import org.krakenapps.console.ShellSession;
import org.krakenapps.script.ScriptContextImpl;
import org.osgi.framework.BundleContext;


public class Handler implements IHandler {
	
	final Logger logger = Logger.getLogger(Handler.class.getName());

	private BundleContext bc;
	private Writer writer;
	private Encoder encoder;
	
	public Handler(BundleContext bc){
		this.bc = bc;
	}
	
	public void sessionOpened(Session session, Writer writer, Encoder encoder) {
		this.writer = writer;
		this.encoder = encoder;
		
		ScriptContextImpl scriptContext = newScriptContext(session);
		ShellSession shellSession = new ShellSession(scriptContext);
		session.setAttribute("session", shellSession);
		
		logger.info("shell opened from: " + session.getChannel().socket().getRemoteSocketAddress());
		
		Charset charset = Charset.forName("utf-8");
		CharsetEncoder charsetEncoder = charset.newEncoder();
		try {
			encoder.encode(session, new TelnetOptionControl(TelnetOptionMessageType.WILL, TelnetOptionCode.Echo), writer);
			encoder.encode(session, new TelnetOptionControl(TelnetOptionMessageType.WILL, TelnetOptionCode.SuppressGoAhead), writer);
			writer.send(new WriteRequest(session.getChannel(), charsetEncoder.encode(CharBuffer.wrap("login as: "))));
		} catch (CharacterCodingException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sessionClosed(Session session) {
		logger.info("console closed from: " + session.getChannel().socket().getRemoteSocketAddress());
	}
	
	public void messageReceived(Session session, Object message) throws IOException {
		try {
			ShellSession shellSession = getShellSession(session);
			shellSession.handleMessage(message);
		} catch (IOException e) {
			if (e.getMessage().equals("quit"))
				session.getChannel().close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private ShellSession getShellSession(Session session) {
		ShellSession shellSession = (ShellSession) session.getAttribute("session");
		return shellSession;
	}
	
	public void exceptionCaught(Session session) {
	}
	
	private ScriptContextImpl newScriptContext(Session session) {
		ScriptContextImpl context = new ScriptContextImpl(bc);
		context.setInputStream(new ConsoleInputStream(context));
		context.setOutputStream(new ConsoleOutputStream(session, writer, encoder));
		return context;
	}

}
