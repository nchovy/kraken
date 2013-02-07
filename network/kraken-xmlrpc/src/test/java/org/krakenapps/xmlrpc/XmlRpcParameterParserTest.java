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

import java.util.Date;
import java.util.Map;

import org.junit.Test;
import org.krakenapps.xmlrpc.DateUtil;
import org.krakenapps.xmlrpc.XmlRpcParameterParser;
import org.krakenapps.xmlrpc.XmlUtil;
import org.w3c.dom.Document;

public class XmlRpcParameterParserTest {
	@Test
	public void testPrettyStringNode() {
		assertEquals("kraken", parseParamNode("<param>\n<value>\n\t  <string>kraken</string>\n</value>\n  </param>"));
	}

	@Test
	public void testParseStringNode() {
		assertEquals("kraken", parseParamNode("<param><value><string>kraken</string></value></param>"));
	}

	@Test
	public void testParseI4Node() {
		assertEquals(-1, parseParamNode("<param><value><i4>-1</i4></value></param>"));
	}

	@Test
	public void testParseIntNode() {
		assertEquals(42, parseParamNode("<param><value><int>42</int></value></param>"));
	}

	@Test
	public void testParseBooleanNode() {
		assertEquals(true, parseParamNode("<param><value><boolean>1</boolean></value></param>"));
	}

	@Test
	public void testParseDoubleNode() {
		assertEquals(3.141592, parseParamNode("<param><value><double>3.141592</double></value></param>"));
	}

	@Test
	public void testParseBase64Node() {
		final String ENCODED_TEXT = "VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=";
		final String ORIGINAL_TEXT = "This is a base64 encoded text";

		byte[] value = (byte[]) parseParamNode("<param><value><base64>" + ENCODED_TEXT + "</base64></value></param>");
		assertEquals(ORIGINAL_TEXT, new String(value));
	}

	@Test
	public void testParseDateNode() {
		Date actual = (Date) parseParamNode("<param><value><dateTime.iso8601>19980717T14:08:55</dateTime.iso8601></value></param>");
		Date expected = DateUtil.create(1998, 7, 17, 14, 8, 55);
		assertEquals(expected, actual);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testParseStructNode() {
		Map<String, Object> map = (Map<String, Object>) parseParamNode("<param><value><struct>"
				+ "<member><name>num</name><value><i4>1234</i4></value></member>"
				+ "<member><name>str</name><value><string>abcd</string></value></member>" + "</struct></value></param>");
		assertEquals(1234, map.get("num"));
		assertEquals("abcd", map.get("str"));
	}

	@Test
	public void testParseArrayNode() {
		Object[] array = (Object[]) parseParamNode("<param><value><array><data>" + "<value><i4>12</i4></value>"
				+ "<value><string>Egypt</string></value>" + "<value><boolean>0</boolean></value>"
				+ "<value><i4>-31</i4></value>" + "</data></array></value></param>");

		assertEquals(4, array.length);
		assertEquals(12, array[0]);
		assertEquals("Egypt", array[1]);
		assertEquals(false, array[2]);
		assertEquals(-31, array[3]);
	}

	private Object parseParamNode(String xml) {
		Document document = XmlUtil.parse(xml);
		return XmlRpcParameterParser.parse(document.getLastChild());
	}
}
