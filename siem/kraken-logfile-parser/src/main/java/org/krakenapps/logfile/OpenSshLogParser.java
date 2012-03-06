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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.krakenapps.log.api.LogParser;

public class OpenSshLogParser implements LogParser {
	@Override
	public Map<String, Object> parse(Map<String, Object> params) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss", Locale.ENGLISH);
		Map<String, Object> m = new HashMap<String, Object>();

		String line = (String) params.get("line");
		Date date = null;
		try {
			date = dateFormat.parse(line);
			Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			c.setTime(date);
			c.set(Calendar.YEAR, year);
			date = c.getTime();
		} catch (ParseException e) {
		}

		String[] tokens = split(line);

		m.put("logtype", "openssh");
		m.put("_time", date);
		m.put("host", tokens[3]);
		m.put("logger", tokens[4].substring(0, tokens[4].length() - 1));

		int bodyPosition = line.indexOf(": ");
		String body = line.substring(bodyPosition + 2);
		m.put("type", "unknown");

		String target = "Accepted password for ";
		int acceptedPasswordPosition = body.indexOf(target);
		if (acceptedPasswordPosition != -1) {
			String[] bodyTokens = split(body);
			m.put("type", "login");
			m.put("result", "success");
			m.put("account", bodyTokens[3]);
			m.put("src_ip", bodyTokens[5]);
			m.put("src_port", bodyTokens[7]);
			m.put("protocol", bodyTokens[8]);
		}

		target = "Failed password for";
		int failedPasswordPosition = body.indexOf(target);
		if (failedPasswordPosition != -1) {
			int invalidUser = body.indexOf("invalid user", target.length());
			int offset = 0;
			if (invalidUser > 0)
				offset += 2;

			String[] bodyTokens = split(body);
			m.put("type", "login");
			m.put("result", "failure");
			m.put("account", bodyTokens[3 + offset]);
			m.put("src_ip", bodyTokens[5 + offset]);
			m.put("src_port", bodyTokens[7 + offset]);
			m.put("protocol", bodyTokens[8 + offset]);
		}

		target = "pam_unix(sshd:";
		int pamPosition = body.indexOf(target);
		if (pamPosition != -1) {
			m.put("type", "login");
			if (body.indexOf("session", target.length()) != -1) {
				int closed = body.indexOf("closed", target.length());
				int offset = 0;

				m.put("category", "session");
				if (closed > 0) {
					offset += 2;

					String[] bodyTokens = split(body);
					m.put("account", bodyTokens[3 + offset]);
					m.put("result", "closed");
				}

				int opened = body.indexOf("opened", target.length());
				offset = 0;

				if (opened > 0) {
					offset += 2;

					String[] bodyTokens = split(body);
					m.put("account", bodyTokens[3 + offset]);
					m.put("result", "opened");

					int uidEnd;
					String uid;
					uidEnd = bodyTokens[5 + offset].indexOf(")");
					uid = bodyTokens[5 + offset].substring(5, uidEnd);

					m.put("uid", Integer.valueOf(uid));
				}
			}
		}

		if (m.get("type") == "unknown")
			return null;

		return m;
	}

	private static String[] split(String source) {
		return split(source, " ");
	}

	private static String[] split(String source, String separater) {
		String[] tokens = source.split(separater);
		int emptyCount = 0;
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].length() == 0)
				emptyCount++;
		}

		// fast return
		if (emptyCount == 0)
			return tokens;

		String[] nonEmptyTokens = new String[tokens.length - emptyCount];

		int index = 0;
		for (int i = 0; i < tokens.length; i++)
			if (tokens[i].length() > 0)
				nonEmptyTokens[index++] = tokens[i];

		return nonEmptyTokens;
	}
}
