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
package org.krakenapps.script;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.krakenapps.ansicode.AnsiEscapeCode;
import org.krakenapps.ansicode.LocalEchoControl;
import org.krakenapps.api.AccountManager;
import org.krakenapps.api.FunctionKeyEvent;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptInputStream;
import org.krakenapps.api.ScriptOutputStream;
import org.krakenapps.api.ScriptSession;
import org.krakenapps.api.WindowSizeEventListener;
import org.krakenapps.console.ConsoleAutoComplete;
import org.krakenapps.console.ConsoleController;
import org.krakenapps.console.ConsoleHistoryManager;
import org.krakenapps.console.QuitHandler;
import org.krakenapps.main.Kraken;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ScriptContextImpl implements ScriptContext {
	private int width = 80;
	private int height = 24;

	private BundleContext bc;
	private Script currentScript;
	private ScriptInputStream inputStream;
	private ScriptOutputStream outputStream;
	private Properties properties;
	private boolean doEcho = true;
	private ScriptSession session;
	private ConsoleController controller;
	private ConsoleHistoryManager history;
	private QuitHandler quit;

	private Set<WindowSizeEventListener> callbacks = new HashSet<WindowSizeEventListener>();

	public ScriptContextImpl(BundleContext bc) {
		this(bc, null);
	}

	public ScriptContextImpl(BundleContext bc, QuitHandler quit) {
		this.bc = bc;
		this.properties = new Properties();
		this.controller = new ConsoleController(this, new ConsoleAutoComplete(Kraken.getContext()));
		this.history = new ConsoleHistoryManager(controller);
		this.controller.setArrowKeyHandler(history);
		this.session = new ScriptSessionImpl(history);
		this.session.setProperty("dir", new File(System.getProperty("user.dir")));
		this.quit = quit;
	}

	@Override
	public AccountManager getAccountManager() {
		ServiceReference ref = bc.getServiceReference(AccountManager.class.getName());
		AccountManager accountManager = (AccountManager) bc.getService(ref);
		return accountManager;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setWindowSize(int width, int height) {
		this.width = width;
		this.height = height;

		for (WindowSizeEventListener callback : callbacks) {
			Thread t = new Thread(new SizeEventCallback(callback, width, height), "WindowSizeEventCallback");
			t.start();
		}
	}

	public ConsoleController getController() {
		return controller;
	}

	public ConsoleHistoryManager getHistoryManager() {
		return history;
	}

	@Override
	public ScriptSession getSession() {
		return session;
	}

	@Override
	public Properties getEnvironmentVariables() {
		return properties;
	}

	@Override
	public Script getCurrentScript() {
		return currentScript;
	}

	@Override
	public void setCurrentScript(Script currentScript) {
		this.currentScript = currentScript;
	}

	@Override
	public ScriptInputStream getInputStream() {
		return inputStream;
	}

	@Override
	public ScriptOutputStream getOutputStream() {
		return outputStream;
	}

	@Override
	public void printPrompt() {
		getOutputStream().print(getSession().getPrompt());
	}

	@Override
	public void print(String value) {
		if (value == null)
			return;

		if (outputStream != null)
			outputStream.print(value);
	}

	@Override
	public void print(Object value) {
		print(value.toString());
	}

	@Override
	public void println() {
		println("");
	}

	@Override
	public void println(String value) {
		if (value == null)
			return;

		print(value);
		print("\r\n");
	}

	@Override
	public void println(Object value) {
		println(value.toString());
	}

	@Override
	public void printf(String format, Object... args) {
		if (outputStream != null)
			outputStream.printf(format, args);
	}

	@Override
	public void print(AnsiEscapeCode ansiCode) {
		if (outputStream != null)
			outputStream.print(ansiCode);
	}

	@Override
	public char read() throws InterruptedException {
		return inputStream.read();
	}

	@Override
	public String readLine() throws InterruptedException {
		return inputStream.readLine();
	}

	@Override
	public String readPassword() throws InterruptedException {
		turnEchoOff();
		try {
			return readLine();
		} finally {
			turnEchoOn();
			println("");
		}
	}

	@Override
	public void setEnvironmentVariables(Properties newProperties) {
		this.properties = newProperties;
	}

	@Override
	public void setInputStream(ScriptInputStream newInputStream) {
		this.inputStream = newInputStream;
	}

	@Override
	public void setOutputStream(ScriptOutputStream newOutputStream) {
		this.outputStream = newOutputStream;
	}

	@Override
	public void turnEchoOn() {
		doEcho = true;
	}

	@Override
	public void turnEchoOff() {
		doEcho = false;
	}

	@Override
	public boolean isEchoOn() {
		return doEcho;
	}

	public void turnLocalEchoOn() {
		print(new LocalEchoControl(LocalEchoControl.Option.Set));
	}

	public void turnLocalEchoOff() {
		print(new LocalEchoControl(LocalEchoControl.Option.Reset));
	}

	@Override
	public void transferInput(char character) {
		inputStream.supplyInput(character);
	}

	@Override
	public void transferInput(FunctionKeyEvent keyEvent) {
		inputStream.supplyFunctionKey(keyEvent);
	}

	@Override
	public void addWindowSizeEventListener(WindowSizeEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("callback must be not null");

		callbacks.add(callback);
	}

	@Override
	public void removeWindowSizeEventListener(WindowSizeEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("callback must be not null");

		callbacks.remove(callback);
	}

	private static class SizeEventCallback implements Runnable {
		private int width;
		private int height;
		private WindowSizeEventListener callback;

		public SizeEventCallback(WindowSizeEventListener callback, int width, int height) {
			this.width = width;
			this.height = height;
			this.callback = callback;
		}

		@Override
		public void run() {
			callback.sizeChanged(width, height);
		}
	}

	@Override
	public void quit() {
		if (quit != null) {
			quit.onQuit();
			quit = null;
		} else
			throw new RuntimeException("quit");
	}
}
