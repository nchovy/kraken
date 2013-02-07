/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.codec;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class Base64Test {

	@Before
	public void start() {

	}

	@Test
	public void testVerified() {
		String targetString = "abcdefghijklmnopqrstuvwxyz1234567890";

		assertEquals("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkw", Base64.encodeString(targetString));
	}

	@Test
	public void testBase64String() {
		String targetString = "abcdefghijklmnopqrstuvwxyz1234567890";
		String resultString = Base64.encodeString(targetString);

		assertEquals(Base64.decodeString(resultString), targetString);
	}

	@Test
	public void testBase64Byte() {
		byte[] byteBuffer = new byte[52];
		int index = 0;
		for (char c = 'A'; c < 'Z'; c++) {
			byteBuffer[index++] = (byte) c;
		}
		for (char c = 'a'; c < 'z'; c++) {
			byteBuffer[index++] = (byte) c;
		}

		String encodingString = Base64.encodeLines(byteBuffer);
		byte[] decodingByte = Base64.decodeLines(encodingString);

		assertEquals(byteBuffer.length, decodingByte.length);

		for (int i = 0; i < byteBuffer.length; i++) {
			assertEquals(byteBuffer[i], decodingByte[i]);
		}

	}
}
