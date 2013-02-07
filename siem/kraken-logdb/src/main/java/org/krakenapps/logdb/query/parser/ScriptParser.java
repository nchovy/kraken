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
import org.krakenapps.logdb.LogQueryScript;
import org.krakenapps.logdb.LogQueryScriptRegistry;
import org.krakenapps.logdb.query.command.Script;
import org.osgi.framework.BundleContext;

public class ScriptParser implements LogQueryCommandParser {
	private BundleContext bc;
	private LogQueryScriptRegistry scriptRegistry;

	public ScriptParser(BundleContext bc, LogQueryScriptRegistry scriptRegistry) {
		this.bc = bc;
		this.scriptRegistry = scriptRegistry;
	}

	@Override
	public String getCommandName() {
		return "script";
	}

	@Override
	public LogQueryCommand parse(LogQueryContext context, String commandString) {
		QueryTokens tokens = QueryTokenizer.tokenize(commandString);
		String name = tokens.firstArg();
		LogQueryScript script = scriptRegistry.newScript("localhost", name, null);
		if (script == null)
			throw new IllegalArgumentException("log script not found: " + name);

		// TODO: parameter passing
		script.init(null);

		List<String> args = tokens.substrings(2, tokens.size() - 1);
		return new Script(bc, script, args.toArray(new String[0]));
	}
}