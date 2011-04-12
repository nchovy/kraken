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
package org.krakenapps.test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.krakenapps.ansicode.AnsiEscapeCode;
import org.krakenapps.api.ScriptOutputStream;
import org.krakenapps.api.TelnetCommand;

public class ConsoleOutputStream implements ScriptOutputStream {
	private Session session;

	CharsetEncoder charsetEncoder;
	Charset charset;
	Writer out;
	Encoder encoder;
	
	public ConsoleOutputStream(Session session, Writer writer, Encoder encoder) {
		this.session = session;
		this.out = writer;
		this.encoder = encoder;
		
		charset = Charset.forName("utf-8");
		charsetEncoder = charset.newEncoder();
		
	}
	
	public ScriptOutputStream print(String value) {
		try {
			encoder.encode(session, value, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public ScriptOutputStream println(String value) {
		try {
			encoder.encode(session, value, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public ScriptOutputStream printf(String format, Object... args) {
		try {
			encoder.encode(session, String.format(normalize(format), args), out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	private String normalize(String s) {
		Pattern p = Pattern.compile("([^\r])\n");
		Matcher m = p.matcher(s);
		if (m.find())
			return m.replaceAll("$1\r\n");

		return s;
	}

	public ScriptOutputStream print(AnsiEscapeCode code) {
		try {
			encoder.encode(session, code, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public ScriptOutputStream print(TelnetCommand command) {
		try {
			encoder.encode(session, command, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return this;
	}
}
