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
package org.krakenapps.mime;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

/**
 * @author mindori
 */
public class MimeHeader {
	private Map<String, Object> headers;

	public MimeHeader() {
		headers = new HashMap<String, Object>();
	}

	public Collection<String> keySet() {
		return Collections.unmodifiableCollection(headers.keySet());
	}

	public String getHeader(String key) {
		if (headers.containsKey(key)) {
			if (headers.get(key) instanceof String) {
				return (String) headers.get(key);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<String> getHeaders(String key) {
		if (headers.containsKey(key)) {
			if (headers.get(key) instanceof List) {
				return (List<String>) headers.get(key);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void put(String key, String value) {
		if (headers.containsKey(key)) {
			if (headers.get(key) instanceof String) {
				/* create new list */
				List<String> values = new ArrayList<String>();
				String val = (String) headers.get(key);
				values.add(val);
				values.add(value);

				headers.remove(key);
				headers.put(key, values);
			} else if (headers.get(key) instanceof List) {
				List<String> values = (List<String>) headers.get(key);
				values.add(value);
			}
		} else
			headers.put(key, value);
	}

	public Charset getHeaderCharset(MimeMessage msg) {
		try {
			String contentType = msg.getContentType();
			Charset utfCharset = Charset.forName("utf-8");
			Charset headerCharset = null;
			
			if (contentType != null) {
				headerCharset = parseCharset(contentType);
				if (headerCharset == null) {
					if (msg.getContent() instanceof Multipart) {
						Multipart mp = (Multipart) msg.getContent();
						for (int i = 0; i < mp.getCount(); i++) {
							BodyPart bp = mp.getBodyPart(i);
							Charset c = parseCharset(bp.getContentType());
							if (c != null) {
								headerCharset = c;
								break;
							}
						}
					}
				}
			}
			if (headerCharset == null)
				headerCharset = utfCharset;
			return headerCharset;
		} catch (Exception e) {
		}
		return null;
	}

	private Charset parseCharset(String contentType) {
		String charsetName = null;
		int begin = contentType.indexOf("charset=");
		if (begin > 0) {
			begin += "charset=".length();
			int end = contentType.indexOf(';', begin);
			if (end < 0)
				end = contentType.length();

			charsetName = contentType.substring(begin, end).trim();
			charsetName = charsetName.replaceAll("\"", "");
			try {
				return Charset.forName(charsetName);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	public MimeHeader decodeHeader(Charset charset, byte[] data) {
		int offset = 0;
		while (offset < data.length) {
			if (data[offset] == 0x0d && data[offset + 1] == 0x0a && data[offset + 2] == 0x0d
					&& data[offset + 3] == 0x0a) {
				break;
			}
			offset++;
		}

		/* decode range: 0 ~ offset - 1 */
		String[] headerLines = new String(data, 0, offset, charset).split("\r\n");

		String key = null;
		String value = null;

		for (String line : headerLines) {
			if (line.startsWith(" ") || line.startsWith("	")) {
				/* merge */
				value += decodeLine(line);
			} else {
				/* add */
				if (key != null) {
					put(key, value);
				}

				int pos = line.indexOf(':');
				if (pos < 0)
					continue;

				key = line.substring(0, pos).trim();
				value = decodeLine(line.substring(pos + 1));
			}
		}

		// last line
		put(key, value);
		return this;
	}

	private String decodeLine(String line) {
		return skipWhitespace(MimeDecoder.decode(line));
	}

	private String skipWhitespace(String line) {
		if (line == null)
			return null;

		for (int i = 0; i < line.length(); i++)
			if (line.charAt(i) != ' ' && line.charAt(i) != '\t') {
				return line.substring(i);
			}

		return line;
	}
}
