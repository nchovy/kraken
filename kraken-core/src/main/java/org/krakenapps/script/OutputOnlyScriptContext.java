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
package org.krakenapps.script;

import java.util.Properties;

import org.krakenapps.ansicode.AnsiEscapeCode;
import org.krakenapps.api.AccountManager;
import org.krakenapps.api.FunctionKeyEvent;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptInputStream;
import org.krakenapps.api.ScriptOutputStream;
import org.krakenapps.api.ScriptSession;
import org.krakenapps.api.WindowSizeEventListener;
import org.slf4j.Logger;

public class OutputOnlyScriptContext implements ScriptContext {
	private Logger logger;

	public OutputOnlyScriptContext(Logger logger) {
		this.logger = logger;
	}

	@Override
	public AccountManager getAccountManager() {
		return null;
	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public void setWindowSize(int width, int height) {
	}

	@Override
	public ScriptSession getSession() {
		return null;
	}

	@Override
	public Script getCurrentScript() {
		return null;
	}

	@Override
	public void setCurrentScript(Script currentScript) {
		// TODO Auto-generated method stub

	}

	@Override
	public ScriptInputStream getInputStream() {
		return null;
	}

	@Override
	public void setInputStream(ScriptInputStream newInputStream) {
	}

	@Override
	public ScriptOutputStream getOutputStream() {
		return null;
	}

	@Override
	public void setOutputStream(ScriptOutputStream newOutputStream) {

	}

	@Override
	public char read() throws InterruptedException {
		return 0;
	}

	@Override
	public String readLine() throws InterruptedException {
		return null;
	}

	@Override
	public String readPassword() throws InterruptedException {
		return null;
	}

	@Override
	public void printPrompt() {
	}

	@Override
	public void println() {
		if (logger == null)
			return;
		logger.info("");
	}

	@Override
	public void println(String value) {
		if (logger == null)
			return;
		logger.info(value);
	}

	@Override
	public void println(Object value) {
		if (logger == null)
			return;
		logger.info(value.toString());
	}

	@Override
	public void print(String value) {
		if (logger == null)
			return;
		logger.info(value);
	}

	@Override
	public void print(AnsiEscapeCode ansiCode) {
		// do nothing
	}

	@Override
	public void print(Object value) {
		if (logger == null)
			return;
		logger.info(value.toString());
	}

	@Override
	public void printf(String format, Object... args) {
		if (logger == null)
			return;
		logger.info(stripNewLineInEnd(String.format(format, args)));
	}

	private String stripNewLineInEnd(String format) {
		if (format.endsWith("\n"))
			return format.substring(0, format.length() - 1);
		else
			return format;
	}

	@Override
	public Properties getEnvironmentVariables() {
		return null;
	}

	@Override
	public void setEnvironmentVariables(Properties newProperties) {

	}

	@Override
	public void transferInput(char ch) {

	}

	@Override
	public void transferInput(FunctionKeyEvent keyEvent) {

	}

	@Override
	public boolean isEchoOn() {
		return false;
	}

	@Override
	public void turnEchoOn() {

	}

	@Override
	public void turnEchoOff() {

	}

	@Override
	public void addWindowSizeEventListener(WindowSizeEventListener callback) {

	}

	@Override
	public void removeWindowSizeEventListener(WindowSizeEventListener callback) {

	}

	@Override
	public void quit() {
	}
}
