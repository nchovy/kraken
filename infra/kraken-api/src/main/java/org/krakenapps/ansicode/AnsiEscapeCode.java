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
package org.krakenapps.ansicode;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public abstract class AnsiEscapeCode {
	public abstract byte[] toByteArray();

	protected byte[] wrapCSI(String codes) {
		Charset utf8 = Charset.forName("utf-8");
		ByteBuffer bb = utf8.encode(codes);
		byte[] codeBytes = new byte[bb.limit()];
		bb.get(codeBytes);
		
		byte[] sequence = new byte[2 + codeBytes.length];
		sequence[0] = (byte) 0x1b; // ESC
		sequence[1] = (byte) 0x5b; // [
		for (int i = 0; i < codeBytes.length; i++) {
			sequence[i + 2] = codeBytes[i];
		}
		return sequence;
	}
}