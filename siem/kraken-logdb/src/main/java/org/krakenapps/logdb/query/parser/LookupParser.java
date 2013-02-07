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

import java.util.List;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryCommandParser;
import org.krakenapps.logdb.LogQueryContext;
import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.LookupHandlerRegistry;
import org.krakenapps.logdb.query.command.Lookup;

public class LookupParser implements LogQueryCommandParser {
	private LookupHandlerRegistry registry;

	public LookupParser(LookupHandlerRegistry registry) {
		this.registry = registry;
	}

	@Override
	public String getCommandName() {
		return "lookup";
	}

	@Override
	public LogQueryCommand parse(LogQueryContext context, String commandString) {
		QueryTokens tokens = QueryTokenizer.tokenize(commandString);

		// find OUTPUT token
		int outputOffset = -1;
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.string(i).equalsIgnoreCase("output")) {
				outputOffset = i;
				break;
			}
		}

		if (outputOffset == -1)
			throw new LogQueryParseException("output-token-not-found", commandString.length());

		List<String> inputTokens = tokens.substrings(1, outputOffset);
		List<String> outputTokens = tokens.substrings(outputOffset + 1);

		String handlerName = tokens.firstArg();

		LookupField src = parseLookupField(inputTokens);
		LookupField dst = parseLookupField(outputTokens);

		Lookup lookup = new Lookup(handlerName, src.first, src.second, dst.first, dst.second);
		lookup.setLogQueryService(registry);
		return lookup;
	}

	private LookupField parseLookupField(List<String> tokens) {
		LookupField field = new LookupField();
		if (tokens.size() == 1) {
			field.first = tokens.get(0);
			field.second = field.first;
			return field;
		}

		if (tokens.size() != 3)
			throw new LogQueryParseException("invalid-looup-field", -1);

		if (!tokens.get(1).equalsIgnoreCase("as"))
			throw new LogQueryParseException("as-token-not-found", -1);

		field.first = tokens.get(0);
		field.second = tokens.get(2);
		return field;
	}

	private class LookupField {
		private String first;
		private String second;
	}
}
