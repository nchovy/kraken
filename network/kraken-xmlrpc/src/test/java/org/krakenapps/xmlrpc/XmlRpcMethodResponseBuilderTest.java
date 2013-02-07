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
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.krakenapps.xmlrpc.DateUtil;
import org.krakenapps.xmlrpc.XmlRpcMethodResponseBuilder;
import org.krakenapps.xmlrpc.XmlUtil;
import org.w3c.dom.Document;

public class XmlRpcMethodResponseBuilderTest {
	@Test
	public void testStringValue() {
		String actual = callBuilder("hello");
		String expected = methodResponse(valueNode("string", "hello"));
		assertEquals(expected, actual);
	}

	@Test
	public void testIntValue() {
		String actual = callBuilder(100);
		String expected = methodResponse(valueNode("i4", "100"));
		assertEquals(expected, actual);
	}

	@Test
	public void testDoubleValue() {
		String actual = callBuilder(2.71828183);
		String expected = methodResponse(valueNode("double", "2.71828183"));
		assertEquals(expected, actual);
	}

	@Test
	public void testBooleanValue() {
		String actual = callBuilder(false);
		String expected = methodResponse(valueNode("boolean", "0"));
		assertEquals(expected, actual);
	}

	@Test
	public void testDateValue() {
		Date date = DateUtil.create(1984, 3, 18, 4, 30, 12);
		String actual = callBuilder(date);
		String expected = methodResponse(valueNode("dateTime.iso8601",
				"19840318T04:30:12"));
		assertEquals(expected, actual);
	}

	@Test
	public void testBase64Value() {
		final String ENCODED_TEXT = "VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=";
		final String ORIGINAL_TEXT = "This is a base64 encoded text";
		String actual = callBuilder(ORIGINAL_TEXT.getBytes());
		String expected = methodResponse(valueNode("base64", ENCODED_TEXT));
		assertEquals(expected, actual);
	}

	@Test
	public void testStructValue() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", "kraken");
		map.put("version", "0.1");
		String actual = callBuilder(map);
		
		// struct/member*/name, value
		String expected = methodResponse(valueNode("struct", memberNode("name",
				valueNode("string", "kraken"))
				+ memberNode("version", valueNode("string", "0.1"))));
		assertEquals(expected, actual);
	}

	@Test
	public void testArrayValue() {
		Object[] array = new Object[] { 12, "Korea" };
		String actual = callBuilder(array);
		
		// array/data/value*
		String expected = methodResponse(valueNode("array", dataNode(valueNode(
				"i4", "12")
				+ valueNode("string", "Korea"))));
		assertEquals(expected, actual);
	}

	private String callBuilder(Object returnValue) {
		Document document = XmlRpcMethodResponseBuilder.result(returnValue);
		return XmlUtil.toXmlString(document);
	}

	private String methodResponse(String valueNode) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<methodResponse><params><param>" + valueNode
				+ "</param></params></methodResponse>";
	}

	private String valueNode(String type, String textContent) {
		return String.format("<value><%s>%s</%s></value>", type, textContent,
				type);
	}

	private String memberNode(String name, String value) {
		return String.format("<member><name>%s</name>%s</member>", name, value);
	}

	private String dataNode(String content) {
		return String.format("<data>%s</data>", content);
	}
}
