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

import java.util.Arrays;
import java.util.Map;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryCommandParser;
import org.krakenapps.logdb.LogQueryContext;
import org.krakenapps.logdb.query.command.Search;
import org.krakenapps.logdb.query.command.Term;

public class SearchParser implements LogQueryCommandParser {

	@Override
	public String getCommandName() {
		return "search";
	}

	@Override
	@SuppressWarnings("unchecked")
	public LogQueryCommand parse(LogQueryContext context, String commandString) {
		ParseResult r = QueryTokenizer.parseOptions(commandString, "search".length());
		Map<String, String> options = (Map<String, String>) r.value;

		Integer limit = null;
		if (options.containsKey("limit"))
			limit = Integer.parseInt(options.get("limit"));

		r = TermParser.parseTerm(commandString, r.next);
		Term term = (Term) r.value;
		return new Search(Arrays.asList(term), limit);
	}
}
