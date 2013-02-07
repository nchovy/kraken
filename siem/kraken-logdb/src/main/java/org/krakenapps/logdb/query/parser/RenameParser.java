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

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryCommandParser;
import org.krakenapps.logdb.LogQueryContext;
import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.query.command.Rename;

public class RenameParser implements LogQueryCommandParser {

	@Override
	public String getCommandName() {
		return "rename";
	}

	@Override
	public LogQueryCommand parse(LogQueryContext context, String commandString) {
		QueryTokens tokens = QueryTokenizer.tokenize(commandString);
		if (tokens.size() < 3)
			throw new LogQueryParseException("as-token-not-found", commandString.length());
		
		if (tokens.size() < 4)
			throw new LogQueryParseException("to-field-not-found", commandString.length());

		if (!tokens.string(2).equalsIgnoreCase("as"))
			throw new LogQueryParseException("invalid-as-position", -1);
		

		String from = tokens.firstArg();
		String to = tokens.lastArg();
		return new Rename(from, to);
	}
}
