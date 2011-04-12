package org.krakenapps.jython.impl;
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
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.krakenapps.api.ScriptContext;

public class JythonOutputStream extends OutputStream {
	private ScriptContext sc;
	
	public JythonOutputStream(ScriptContext sc) {
		this.sc = sc;
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		String s = new String(b, off, len, Charset.forName("utf-8"));
		sc.println(s);
	}

	@Override
	public void write(int b) throws IOException {
		// TODO Auto-generated method stub

	}

}
