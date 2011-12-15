/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.logfile;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.krakenapps.log.api.LogParser;

public class ApacheWebLogParser implements LogParser {
	private String logFormat;
	private final static Map<String, FormatInfo> formats = new HashMap<String, FormatInfo>();

	static class FormatInfo {
		Class<?> clazz;
		String name;

		public FormatInfo(Class<?> clazz, String name) {
			this.clazz = clazz;
			this.name = name;
		}
	}

	static {
		set("a", InetAddress.class, "client_ip");
		set("A", InetAddress.class, "server_ip");
		set("B", Integer.class, "resp_bytes");
		set("b", Integer.class, "resp_bytes_clf");
		set("C", String.class, "cookie");
		set("D", Integer.class, "duration_msec");
		set("e", String.class, "env");
		set("f", String.class, "file");
		set("h", InetAddress.class, "remote_host");
		set("H", String.class, "protocol");
		set("i", String.class, "req_header");
		set("l", String.class, "login");
		set("m", String.class, "method");
		set("n", String.class, "note");
		set("o", String.class, "resp_header");
		set("P", Integer.class, "pid");
		set("p", Integer.class, "server_port");
		set("q", String.class, "query");
		set("r", String.class, "request");
		set("s", Integer.class, "status");
		set("t", Date.class, "date");
		set("T", Integer.class, "duration_sec");
		set("u", String.class, "user");
		set("U", String.class, "url");
		set("v", String.class, "canonical_name");
		set("V", String.class, "server_name");
		set("X", String.class, "connection");
		set("I", Integer.class, "rcvd");
		set("O", Integer.class, "sent");
	}

	private static void set(String descriptor, Class<?> clazz, String name) {
		formats.put(descriptor, new FormatInfo(clazz, name));
	}

	public ApacheWebLogParser() {
		// "common" log format
		// this("%h %l %u %t \"%r\" %>s %b");
		this("%h %l %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\"");
	}

	public ApacheWebLogParser(String logFormat) {
		int formatBegin;
		char f;
		boolean fFlag = false;
		char delimiter = 0;

		for (formatBegin = 0; formatBegin < logFormat.length(); formatBegin++) {
			f = logFormat.charAt(formatBegin);

			if (f == '%') {
				fFlag = true;
			} else if (fFlag == true) {
				String token = Character.toString(f);
				if (f == '>')
					continue;
				else if (f == '{') {
					while (f != '}') {
						formatBegin++;
						f = logFormat.charAt(formatBegin);
					}
					continue;
				}

				if (!formats.containsKey(token))
					throw new IllegalArgumentException();

				if (formatBegin + 1 < logFormat.length()) {
					formatBegin++;
					delimiter = logFormat.charAt(formatBegin);
				} else
					break;

				if (delimiter == '%')
					throw new IllegalArgumentException();

				fFlag = false;
			}
		}

		this.logFormat = logFormat;
	}

	public Map<String, Object> parse(String line) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("line", line);
		return parse(params);
	}

	@Override
	public Map<String, Object> parse(Map<String, Object> params) {
		String line = (String) params.get("line");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("logtype", "httpd");

		int formatBegin;
		int lineBegin, lineEnd = 0;
		boolean fFlag = false;

		char delimiter = 0;
		FormatInfo format = null;
		String content = null;

		for (formatBegin = 0, lineBegin = 0; formatBegin < logFormat.length(); formatBegin++) {

			char f = logFormat.charAt(formatBegin);

			if (f == '%') {
				fFlag = true;
				continue;
			} else if (fFlag == true) {
				if (f == '>')
					continue;
				else if (f == '{') {
					int begin = formatBegin + 1;
					int end;
					while (f != '}') {
						formatBegin++;
						f = logFormat.charAt(formatBegin);
					}
					end = formatBegin;

					content = logFormat.substring(begin, end).replace('-', '_').toLowerCase(Locale.ENGLISH);
					continue;
				}

				String token = Character.toString(f); // casting

				if (formats.containsKey(token)) {
					format = formats.get(token);
				} else
					throw new IllegalArgumentException();

				// Get Delimiter
				if (format.clazz == Date.class) {
					delimiter = ']';
					lineBegin++;
				} else {
					if (formatBegin + 1 < logFormat.length()) {
						delimiter = logFormat.charAt(formatBegin + 1);
						if (delimiter == '%') {
							throw new IllegalArgumentException();
						}
					} else
						delimiter = ' ';
				}

				lineEnd = lineBegin;

				char l = line.charAt(lineEnd);
				while (l != delimiter) {
					lineEnd++;
					if (lineEnd < line.length())
						l = line.charAt(lineEnd);
					else
						break;
				}

				fFlag = false;
			} else { // input format = "ab %h";// line = "ab 10.0.1.17";
				if (line.charAt(lineBegin) == logFormat.charAt(formatBegin)) {
					lineBegin++;
				} else {
					// wrong line variable
					throw new IllegalArgumentException();
				}
				continue;
			}

			// Mapping
			String key = Character.toString(f);
			Object value = null;

			if (format.clazz == String.class) {
				value = line.substring(lineBegin, lineEnd);
			} else if (format.clazz == Integer.class) {
				if (line.substring(lineBegin, lineEnd).equals("-"))
					value = null;
				else
					value = Integer.valueOf(line.substring(lineBegin, lineEnd));
			} else if (format.clazz == InetAddress.class) {
				try {
					value = InetAddress.getByName(line.substring(lineBegin, lineEnd));
				} catch (UnknownHostException e) {
					return null;
				}
			} else { // type == Date.class
				value = parseDate(line.substring(lineBegin, lineEnd));
				lineEnd++;
			}

			lineBegin = lineEnd;

			if (value == null || value.equals("-") || value.equals("")) {
				if (!key.equals("X"))
					value = null;
			}

			if (content != null) {
				key = content;
				content = null;
				m.put(key, value);
			} else {
				m.put(format.name, value);
			}
		}
		return m;
	}

	private Date parseDate(String time) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
		try {
			return dateFormat.parse(time);
		} catch (ParseException e) {
			return null;
		}
	}
}
