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
import java.util.Map;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryCommandParser;
import org.krakenapps.logdb.LogQueryContext;
import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.query.command.Sort;
import org.krakenapps.logdb.query.command.Sort.SortField;

public class SortParser implements LogQueryCommandParser {

	@Override
	public String getCommandName() {
		return "sort";
	}

	@Override
	@SuppressWarnings("unchecked")
	public LogQueryCommand parse(LogQueryContext context, String commandString) {
		ParseResult r = QueryTokenizer.parseOptions(commandString, "sort".length());
		Map<String, String> options = (Map<String, String>) r.value;

		Integer count = null;
		if (options.containsKey("limit"))
			count = Integer.parseInt(options.get("limit"));

		ArrayList<SortField> fields = new ArrayList<SortField>();

		int next = r.next;
		try {
			while (true) {
				r = QueryTokenizer.nextString(commandString, r.next, ',');
				String token = (String) r.value;
				boolean asc = true;
				char sign = token.charAt(0);
				if (sign == '-') {
					token = token.substring(1);
					asc = false;
				} else if (sign == '+') {
					token = token.substring(1);
				}

				SortField field = new SortField(token, asc);
				fields.add(field);
				next = r.next;

				if (commandString.length() == r.next)
					break;
			}

			return new Sort(count, fields.toArray(new SortField[0]));
		} catch (LogQueryParseException e) {
			if (e.getType().equals("need-string-token"))
				throw new LogQueryParseException("need-column", next);
			throw e;
		}
	}
}
