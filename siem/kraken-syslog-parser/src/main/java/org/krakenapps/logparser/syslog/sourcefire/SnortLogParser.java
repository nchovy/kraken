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
package org.krakenapps.logparser.syslog.sourcefire;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.log.api.LogParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnortLogParser implements LogParser {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public Map<String, Object> parse(Map<String, Object> params) {
		Map<String, Object> m = new HashMap<String, Object>();
		String msg = (String) params.get("message");
		try {
			if (!msg.contains("snort["))
				return null;

			Token pid = nextToken(msg, 0, '[', ']');
			Token numbers = nextToken(msg, pid.end, '[', ']');
			Token proto = nextToken(msg, numbers.end, '{', '}');
			if (proto.value.startsWith("PROTO:")) {
				proto.value = TextTokenizer.split(proto.value, ":")[1];
			}

			m.put("pid", Integer.parseInt(pid.value));

			String[] numberTokens = TextTokenizer.split(numbers.value, ":");

			m.put("gid", Integer.parseInt(numberTokens[0]));
			m.put("sid", Integer.parseInt(numberTokens[1]));
			m.put("rev", Integer.parseInt(numberTokens[2]));
			m.put("proto", proto.value);

			int endOfRule = msg.indexOf("{", numbers.end);
			int endOfRule2 = msg.indexOf("[", numbers.end);
			if ((endOfRule2 < endOfRule) && endOfRule2 > 0)
				endOfRule = endOfRule2;

			if (msg.charAt(endOfRule - 1) == ' ')
				endOfRule -= 1;

			String newMsg = msg.substring(numbers.end + 2, endOfRule);
			m.put("msg", newMsg);

			String conversation = msg.substring(proto.end + 1);
			String[] conversationTokens = TextTokenizer.split(conversation, "->");
			String[] srcPair = TextTokenizer.split(conversationTokens[0], ":");
			String[] dstPair = TextTokenizer.split(conversationTokens[1], ":");

			InetAddress srcIp = InetAddress.getByName(srcPair[0].trim());
			InetAddress dstIp = InetAddress.getByName(dstPair[0].trim());

			if (srcPair.length == 2)
				m.put("src_port", Integer.parseInt(srcPair[1].trim()));

			if (dstPair.length == 2)
				m.put("dst_port", Integer.parseInt(dstPair[1].trim()));

			m.put("src_ip", srcIp);
			m.put("dst_ip", dstIp);

			parseAllMetadata(m, msg, endOfRule);

			return m;
		} catch (UnknownHostException e) {
			logger.warn("snort syslog parser: ip parse error [{}]", m);
			return null;
		} catch (Exception e) {
			logger.warn("snort syslog parser: parse error [{}]", m);
			return null;
		}
	}

	private void parseAllMetadata(Map<String, Object> m, String msg, int offset) {
		if (offset >= msg.length())
			return;

		int begin = msg.indexOf("[", offset);
		if (begin < 0)
			return;

		int end = msg.indexOf("]", begin);
		if (end < 0)
			return;

		String metadata = msg.substring(begin + 1, end);
		String[] tokens = metadata.split(":");
		String key = tokens[0];
		String value = tokens[1];

		if (key.equals("Priority")) {
			int priority = Integer.parseInt(value.trim());
			m.put("priority", priority);
		} else if (key.equals("Classification")) {
			m.put("class", value.trim());
		}

		parseAllMetadata(m, msg, end);
	}

	private Token nextToken(String target, int offset, char openChar, char closeChar) {
		if (offset >= target.length())
			return null;

		Token token = new Token();
		token.begin = target.indexOf(openChar, offset);
		if (token.begin < 0)
			return null;

		token.end = target.indexOf(closeChar, token.begin);
		if (token.end < 0)
			return null;

		token.value = target.substring(token.begin + 1, token.end);
		return token;
	}

	private class Token {
		private int begin;
		private int end;
		private String value;
	}
}
