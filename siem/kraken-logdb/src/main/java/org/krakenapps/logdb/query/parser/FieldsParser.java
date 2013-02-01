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

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryCommandParser;
import org.krakenapps.logdb.LogQueryContext;
import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.query.command.Fields;

public class FieldsParser implements LogQueryCommandParser {

	@Override
	public String getCommandName() {
		return "fields";
	}

	@Override
	public LogQueryCommand parse(LogQueryContext context, String commandString) {
		QueryTokens tokens = QueryTokenizer.tokenize(commandString);

		List<String> fields = new ArrayList<String>();
		List<String> args = tokens.substrings(1);

		boolean selector = true;
		if (args.get(0).equals("-")) {
			selector = false;
			args.remove(0);
		}

		if (args.size() == 0)
			throw new LogQueryParseException("no-field-args", -1);

		for (String t : args) {
			String[] csv = t.split(",");
			for (String s : csv)
				fields.add(s);
		}

		return new Fields(fields, selector);
	}
}
