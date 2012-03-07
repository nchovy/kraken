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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.SyntaxProvider;
import org.krakenapps.logdb.impl.ResourceManager;

@Component(name = "logdb-syntax-provider")
@Provides
public class SyntaxProviderImpl implements SyntaxProvider {
	@Requires
	private ResourceManager resman;

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
	public Set<String> getParserNames() {
		Set<String> names = new HashSet<String>();
		for (LogQueryParser parser : queryParsers)
			names.add(parser.getClass().getSimpleName());
		return names;
	}

	@Override
	public LogQueryCommand eval(LogQuery logQuery, String query) throws ParseException {
		LogQueryCommand token = (LogQueryCommand) syntax.eval(query);
		token.setQueryString(query);
		token.setLogQuery(logQuery);
		token.setExecutorService(resman.getExecutorService());
		return token;
	}

	@Override
	public void addParser(Class<? extends LogQueryParser> cls, ResourceManager resman) {
		LogQueryParser parser = createInstance(cls, resman);
		if (parser != null)
			addParser(parser);
	}

	@Override
	public void addParser(LogQueryParser parser) {
		queryParsers.add(parser);
		rebuild();
	}

	@Override
	public void removeParser(Class<? extends LogQueryParser> cls) {
		List<LogQueryParser> removes = new ArrayList<LogQueryParser>();
		for (LogQueryParser parser : queryParsers) {
			if (cls.equals(parser.getClass()))
				removes.add(parser);
		}
		removeParsers(removes);
	}

	@Override
	public void removeParser(LogQueryParser parser) {
		queryParsers.remove(parser);
	}

	@Override
	public void addParsers(Collection<Class<? extends LogQueryParser>> clzs, ResourceManager resman) {
		for (Class<? extends LogQueryParser> cls : clzs) {
			LogQueryParser parser = createInstance(cls, resman);
			if (parser != null)
				queryParsers.add(parser);
		}
		rebuild();
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
		for (LogQueryParser qp : queryParsers) {
			if (qp != null)
				qp.addSyntax(newSyntax);
		}
		this.syntax = newSyntax;
	}

	private LogQueryParser createInstance(Class<? extends LogQueryParser> cls, ResourceManager resman) {
		try {
			try {
				return cls.getConstructor(ResourceManager.class).newInstance(resman);
			} catch (NoSuchMethodException e) {
				return cls.newInstance();
			}
		} catch (Exception e) {
			return null;
		}
	}
}
