/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logdb.query.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryCommandParser;
import org.krakenapps.logdb.LogQueryContext;
import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.query.command.Rex;

public class RexParser implements LogQueryCommandParser {

	@Override
	public String getCommandName() {
		return "rex";
	}

	@Override
	public LogQueryCommand parse(LogQueryContext context, String commandString) {

		// extract field names and remove placeholder
		List<String> names = new ArrayList<String>();

		ParseResult r = QueryTokenizer.parseOptions(commandString, "rex".length());
		@SuppressWarnings("unchecked")
		Map<String, String> options = (Map<String, String>) r.value;

		String field = options.get("field");
		if (field == null)
			throw new LogQueryParseException("field-not-found", commandString.length());

		Pattern placeholder = Pattern.compile("\\(\\?<(.*?)>(.*?)\\)");
		String regexToken = commandString.substring(r.next);
		System.out.println("before: " + regexToken);
		if (!QueryTokenizer.isQuoted(regexToken))
			throw new LogQueryParseException("invalid-regex", commandString.length());

		regexToken = QueryTokenizer.removeQuotes(regexToken);
		System.out.println("after: " + regexToken);
		regexToken = toNonCapturingGroup(regexToken);

		Matcher matcher = placeholder.matcher(regexToken);
		while (matcher.find())
			names.add(matcher.group(1));

		while (true) {
			matcher = placeholder.matcher(regexToken);
			if (!matcher.find())
				break;

			// supporess special meaning of $ and \
			String quoted = Matcher.quoteReplacement("(" + matcher.group(2) + ")");
			regexToken = matcher.replaceFirst(quoted);
		}

		System.out.println("regex: " + regexToken);
		Pattern p = Pattern.compile(regexToken);
		return new Rex(field, p, names.toArray(new String[0]));
	}

	private String toNonCapturingGroup(String s) {
		StringBuilder sb = new StringBuilder();

		char last = ' ';
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (last == '(' && c != '?')
				sb.append("?:");
			sb.append(c);
			last = c;
		}

		return sb.toString();
	}

}
