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
package org.krakenapps.xmlrpc;

import static org.junit.Assert.*;

import java.nio.charset.Charset;

import org.junit.Test;

/* Test set from Wikipedia (http://en.wikipedia.org/wiki/Base64) */
public class Base64Test {
	@Test
	public void encode() {
		byte[] source = getBytes("Man");
		byte[] actual = XmlUtil.encodeBase64(source);
		byte[] expected = getBytes("TWFu");
		assertArrayEquals(expected, actual);

		source = getBytes("Man is distinguished, not only by his reason, but by this singular "
				+ "passion from other animals, which is a lust of the mind, that by a perseverance "
				+ "of delight in the continued and indefatigable generation of knowledge, exceeds "
				+ "the short vehemence of any carnal pleasure.");
		actual = XmlUtil.encodeBase64(source);
		expected = getBytes("TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0"
				+ "aGlzIHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2YgdG"
				+ "hlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGludWVkIGFu"
				+ "ZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRoZSBzaG9ydCB2ZW"
				+ "hlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=");
		assertArrayEquals(expected, actual);
	}

	@Test
	public void decode() {
		byte[] source = getBytes("TWFu");
		byte[] actual = XmlUtil.decodeBase64(source);
		byte[] expected = getBytes("Man");
		assertArrayEquals(expected, actual);

		source = getBytes("TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0"
				+ "aGlzIHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2YgdG"
				+ "hlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGludWVkIGFu"
				+ "ZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRoZSBzaG9ydCB2ZW"
				+ "hlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=");
		actual = XmlUtil.decodeBase64(source);
		expected = getBytes("Man is distinguished, not only by his reason, but by this singular "
				+ "passion from other animals, which is a lust of the mind, that by a perseverance "
				+ "of delight in the continued and indefatigable generation of knowledge, exceeds "
				+ "the short vehemence of any carnal pleasure.");
		assertArrayEquals(expected, actual);
	}

	private byte[] getBytes(String str) {
		return str.getBytes(Charset.forName("ascii"));
	}
}
