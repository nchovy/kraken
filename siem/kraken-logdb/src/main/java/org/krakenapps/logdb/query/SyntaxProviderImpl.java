/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.logdb.query;

import java.text.ParseException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.SyntaxProvider;

@Component(name = "logdb-syntax-provider")
@Provides
public class SyntaxProviderImpl implements SyntaxProvider {
	private Syntax syntax;
	private CopyOnWriteArraySet<LogQueryParser> queryParsers;

	@Validate
	public void start() {
		queryParsers = new CopyOnWriteArraySet<LogQueryParser>();
	}

	@Override
	public Syntax getSyntax() {
		return syntax;
	}

	@Override
	public LogQueryCommand eval(LogQuery logQuery, String query) throws ParseException {
		LogQueryCommand token = (LogQueryCommand) syntax.eval(query);
		token.setQueryString(query);
		token.setLogQuery(logQuery);
		return token;
	}

	@Override
	public void addParsers(Collection<? extends LogQueryParser> parsers) {
		queryParsers.addAll(parsers);
		rebuild();
	}

	@Override
	public void removeParsers(Collection<? extends LogQueryParser> parsers) {
		queryParsers.removeAll(parsers);
		rebuild();
	}

	private void rebuild() {
		Syntax newSyntax = new Syntax();
		for (LogQueryParser qp : queryParsers)
			if (qp != null)
				qp.addSyntax(newSyntax);

		this.syntax = newSyntax;
	}
}
