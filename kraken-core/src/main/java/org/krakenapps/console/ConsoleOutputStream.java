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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.mina.core.session.IoSession;
import org.krakenapps.ansicode.AnsiEscapeCode;
import org.krakenapps.api.ScriptOutputStream;
import org.krakenapps.api.TelnetCommand;

public class ConsoleOutputStream implements ScriptOutputStream {
	private IoSession session;

	public ConsoleOutputStream(IoSession session) {
		this.session = session;
	}

	@Override
	public ScriptOutputStream print(String value) {
		session.write(normalize(value));
		return this;
	}

	@Override
	public ScriptOutputStream println(String value) {
		session.write(normalize(value) + "\r\n");
		return this;
	}

	@Override
	public ScriptOutputStream printf(String format, Object... args) {
		session.write(String.format(normalize(format), args));
		return this;
	}

	private String normalize(String s) {
		Pattern p = Pattern.compile("([^\r])\n");
		Matcher m = p.matcher(s);
		if (m.find())
			return m.replaceAll("$1\r\n");

		return s;
	}

	@Override
	public ScriptOutputStream print(AnsiEscapeCode code) {
		session.write(code);
		return this;
	}

	@Override
	public ScriptOutputStream print(TelnetCommand command) {
		session.write(command);
		return this;
	}

}
