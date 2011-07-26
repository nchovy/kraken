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
package org.krakenapps.api;

import java.util.Properties;

import org.krakenapps.ansicode.AnsiEscapeCode;

public interface ScriptContext {
	AccountManager getAccountManager();
	
	void quit();

	int getWidth();

	int getHeight();

	void setWindowSize(int width, int height);

	ScriptSession getSession();

	Script getCurrentScript();

	void setCurrentScript(Script currentScript);

	ScriptInputStream getInputStream();

	void setInputStream(ScriptInputStream newInputStream);

	ScriptOutputStream getOutputStream();

	void setOutputStream(ScriptOutputStream newOutputStream);

	char read() throws InterruptedException;

	String readLine() throws InterruptedException;

	String readPassword() throws InterruptedException;

	void printPrompt();
	
	void println();

	void println(String value);

	void println(Object value);

	void print(String value);

	void print(AnsiEscapeCode ansiCode);

	void print(Object value);

	void printf(String format, Object... args);

	Properties getEnvironmentVariables();

	void setEnvironmentVariables(Properties newProperties);

	void transferInput(char ch);

	void transferInput(FunctionKeyEvent keyEvent);

	boolean isEchoOn();

	void turnEchoOn();

	void turnEchoOff();

	void addWindowSizeEventListener(WindowSizeEventListener callback);

	void removeWindowSizeEventListener(WindowSizeEventListener callback);
}
