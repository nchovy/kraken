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
package org.krakenapps.xmlrpc;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlRpcBuilderUtil {
	@SuppressWarnings("unchecked")
	public static Element buildValueElement(Document document, Object target) {
		Element value = document.createElement("value");
		if (target instanceof String) {
			value.appendChild(createElement(document, "string", target.toString()));
		} else if (target instanceof Integer) {
			value.appendChild(createElement(document, "i4", target.toString()));
		} else if (target instanceof Double) {
			value.appendChild(createElement(document, "double", target.toString()));
		} else if (target instanceof Boolean) {
			value.appendChild(createElement(document, "boolean", booleanToString((Boolean) target)));
		} else if (target instanceof Date) {
			value.appendChild(createElement(document, "dateTime.iso8601", dateToString((Date) target)));
		} else if (target instanceof byte[]) {
			value.appendChild(createElement(document, "base64", base64ToString((byte[]) target)));
		} else if (target instanceof Map) {
			value.appendChild(structToElement(document, (Map<String, Object>) target));
		} else if (target instanceof Collection<?>) {
			value.appendChild(listToElement(document, (Collection<?>) target));
		} else if (target instanceof Object[]) {
			value.appendChild(arrayToElement(document, (Object[]) target));
		}
		return value;
	}

	private static String booleanToString(Boolean b) {
		String content = null;
		if (b.booleanValue())
			content = "1";
		else
			content = "0";
		return content;
	}

	private static String dateToString(Date d) {
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
		return f.format(d);
	}

	private static String base64ToString(byte[] binary) {
		return new String(XmlUtil.encodeBase64(binary));
	}

	private static Element structToElement(Document document, Map<String, Object> map) {
		Element structElement = document.createElement("struct");
		for (String key : map.keySet()) {
			Element memberElement = document.createElement("member");
			Element nameElement = document.createElement("name");
			nameElement.setTextContent(key);
			Element valueElement = buildValueElement(document, map.get(key));
			memberElement.appendChild(nameElement);
			memberElement.appendChild(valueElement);
			structElement.appendChild(memberElement);
		}
		return structElement;
	}

	private static Element arrayToElement(Document document, Object[] array) {
		return listToElement(document, Arrays.asList(array));
	}

	private static Element listToElement(Document document, Collection<?> collection) {
		Element arrayElement = document.createElement("array");
		Element dataElement = document.createElement("data");
		for (Object value : collection) {
			dataElement.appendChild(buildValueElement(document, value));
		}
		arrayElement.appendChild(dataElement);
		return arrayElement;
	}

	private static Element createElement(Document document, String name, String textContent) {
		Element element = document.createElement(name);
		element.setTextContent(textContent);
		return element;
	}

}
