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
import org.krakenapps.logdb.query.aggregator.AggregationField;
import org.krakenapps.logdb.query.command.Stats;

public class StatsParser implements LogQueryCommandParser {
	private static final String COMMAND = "stats";
	private static final String BY = " by ";

	@Override
	public String getCommandName() {
		return COMMAND;
	}

	@Override
	public LogQueryCommand parse(LogQueryContext context, String commandString) {
		// stats <aggregation function holder> by <stats-fields>

		String aggsPart = commandString.substring(COMMAND.length());
		List<String> clauses = new ArrayList<String>();

		// parse clauses
		int byPos = QueryTokenizer.findKeyword(commandString, BY, 0);
		if (byPos > 0) {
			aggsPart = commandString.substring(COMMAND.length(), byPos);
			String clausePart = commandString.substring(byPos + BY.length());

			if (clausePart.trim().endsWith(","))
				throw new LogQueryParseException("missing-clause", commandString.length());

			// trim
			for (String clause : clausePart.split(","))
				clauses.add(clause.trim());
		}

		// parse aggregations
		List<String> aggTerms = QueryTokenizer.parseByComma(aggsPart);
		List<AggregationField> fields = new ArrayList<AggregationField>();

		for (String aggTerm : aggTerms) {
			AggregationField field = AggregationParser.parse(aggTerm);
			fields.add(field);
		}

		return new Stats(fields, clauses);
	}

}
