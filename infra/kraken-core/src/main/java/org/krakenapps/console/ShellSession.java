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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.api.AccountManager;
import org.krakenapps.api.FunctionKeyEvent;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptOutputStream;
import org.krakenapps.api.ScriptSession;
import org.krakenapps.api.FunctionKeyEvent.KeyCode;
import org.krakenapps.main.Kraken;
import org.krakenapps.script.ScriptContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellSession {
	public static final String KRAKEN_PROMPT = "kraken> ";

	final Logger logger = LoggerFactory.getLogger(ShellSession.class.getName());

	private Map<String, Object> attributes;
	private ScriptContextImpl sc;
	private String lastChar = null;

	public ShellSession(ScriptContextImpl scriptContext) {
		this.attributes = new HashMap<String, Object>();
		this.sc = scriptContext;
	}

	public ScriptContext getScriptContext() {
		return sc;
	}

	public void printBanner() {
		sc.getOutputStream().println(Kraken.BANNER);
	}

	public void handleMessage(Object message) throws InterruptedException, IOException {
		if (ignoreLF(message))
			return;

		if (sc.getCurrentScript() == null)
			processShell(message);
		else
			supplyInputToScript(message);
	}

	// normalize CRLF
	private boolean ignoreLF(Object message) {
		if (!(message instanceof String))
			return false;

		String c = (String) message;
		if (lastChar != null && lastChar.equals("\r") && c.equals("\n")) {
			lastChar = c;
			return true;
		}

		lastChar = c;
		return false;
	}

	private void supplyInputToScript(Object message) {
		if (message instanceof String) {
			String character = (String) message;
			sc.transferInput(character.charAt(0));
		} else if (message instanceof FunctionKeyEvent) {
			FunctionKeyEvent keyEvent = (FunctionKeyEvent) message;
			sc.transferInput(keyEvent);
		} else {
			throw new AssertionError("not supported.");
		}
	}

	private void processShell(Object message) throws InterruptedException, IOException {
		ConsoleController controller = sc.getController();
		if (message instanceof FunctionKeyEvent) {
			FunctionKeyEvent ev = (FunctionKeyEvent) message;

			// suppress function key while logon
			if (attributes.get("principal") == null && !ev.isPressed(KeyCode.BACKSPACE))
				return;

			controller.onFunctionKeyPressed(ev);
			return;
		}

		ScriptOutputStream out = sc.getOutputStream();
		controller.onCharacterInput((String) message);

		if (controller.hasLine() == false)
			return;

		String line = controller.getLine();

		if (attributes.get("principal") == null) {
			String input = line.replaceAll("\r\n", "");
			ScriptSession session = sc.getSession();

			if (session.getProperty("kraken.user") == null) {
				session.setProperty("kraken.user", input);
				out.print("password: ");
				sc.turnEchoOff();
				return;
			} else {
				String name = (String) session.getProperty("kraken.user");

				AccountManager accountManager = sc.getAccountManager();
				if (accountManager.verifyPassword(name, input)) {
					setPrincipal(name);
					sc.turnEchoOn();
					out.print("\r\n");

					printBanner();
					sc.printPrompt();
					return;
				} else {
					out.print("\r\nAccess denied\r\n");
					Thread.sleep(2000);
					out.print("\r\npassword: ");
					return;
				}
			}
		}

		if (line.trim().length() == 0) {
			sc.printPrompt();
			return;
		}

		if (handleEmbeddedCommands(out, line))
			return;

		sc.getHistoryManager().pushLine(line);

		if (logger.isDebugEnabled())
			logger.debug("message received: [" + line + "]");

		try {
			runScript(line);
			return;
		} catch (InstantiationException e) {
			out.print("Script instantiation failed.\r\n");
		} catch (IllegalAccessException e) {
			out.print("Script constructor must have a public access.\r\n");
		} catch (NullPointerException e) {
			out.print("syntax error.\r\n");
		}

		sc.printPrompt();
	}

	public void setPrincipal(String name) {
		attributes.put("principal", name);
	}

	private boolean handleEmbeddedCommands(ScriptOutputStream out, String line) throws IOException {
		line = line.trim();

		if ((line.equals("quit") || line.equals("exit")))
			sc.quit();

		// putty send only CR at ssh mode when you hit enter.
		if (line.equals("\r") || line.equals("\r\n")) {
			sc.printPrompt();
			return true;
		}

		return false;
	}

	private void runScript(String line) throws InstantiationException, IllegalAccessException {
		Thread t = new Thread(new ScriptRunner(sc, line), "Kraken Script Runner [" + line.trim() + "]");
		t.start();
	}
}
