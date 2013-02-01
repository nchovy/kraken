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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryCommandParser;
import org.krakenapps.logdb.LogQueryContext;
import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.query.command.OutputCsv;

public class OutputCsvParser implements LogQueryCommandParser {

	@Override
	public String getCommandName() {
		return "outputcsv";
	}

	@Override
	public LogQueryCommand parse(LogQueryContext context, String commandString) {
		if (commandString.trim().endsWith(","))
			throw new LogQueryParseException("missing-field", commandString.length());
		
		QueryTokens tokens = QueryTokenizer.tokenize(commandString);
		List<String> fields = new ArrayList<String>();
		String csvPath = tokens.firstArg();

		List<QueryToken> fieldTokens = tokens.subtokens(2, tokens.size());
		for (QueryToken t : fieldTokens) {
			StringTokenizer tok = new StringTokenizer(t.token, ",");
			while (tok.hasMoreTokens())
				fields.add(tok.nextToken().trim());
		}
		
		if (fields.size() == 0)
			throw new LogQueryParseException("missing-field", commandString.length());

		File csvFile = new File(csvPath);
		if (csvFile.exists())
			throw new IllegalStateException("csv file exists: " + csvFile.getAbsolutePath());

		try {
			if (csvFile.getParentFile() != null)
				csvFile.getParentFile().mkdirs();
			return new OutputCsv(csvFile, fields);
		} catch (IOException e) {
			throw new LogQueryParseException("io-error", -1, e.getMessage());
		}

	}
}
