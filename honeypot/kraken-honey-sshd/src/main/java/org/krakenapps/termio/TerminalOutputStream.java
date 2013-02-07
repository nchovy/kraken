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
package org.krakenapps.termio;

import java.io.IOException;
import java.io.OutputStream;

public class TerminalOutputStream extends OutputStream {
	private OutputStream out;

	public TerminalOutputStream(OutputStream out) {
		this.out = out;
	}

	public void print(String s) throws IOException {
		out.write(s.getBytes());
		out.flush();
	}

	public void println(String line) throws IOException {
		out.write((line + "\r\n").getBytes());
		out.flush();
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

}
