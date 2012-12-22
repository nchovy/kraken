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
package org.krakenapps.logdb.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.query.StringPlaceholder;
import org.krakenapps.logdb.query.command.Function;
import org.krakenapps.logdb.query.command.Stats;

public class StatsParser implements LogQueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("stats2", this, k("stats2 "), ref("option"), ref("function"), option(k("by "), ref("stats_field")));
		syntax.add("stats_field", new StatsFieldParser(), new StringPlaceholder(new char[] { ' ', ',' }),
				option(ref("stats_field")));
		syntax.addRoot("stats2");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Binding b) {
		List<String> keyFields = null;
		Function[] func = ((List<Function>) b.getChildren()[2].getValue()).toArray(new Function[0]);

		if (b.getChildren().length < 4)
			keyFields = new ArrayList<String>();
		else
			keyFields = (List<String>) b.getChildren()[3].getChildren()[1].getValue();

		return new Stats(keyFields, func);
	}

	public class StatsFieldParser implements Parser {
		@Override
		public Object parse(Binding b) {
			List<String> fields = new ArrayList<String>();
			parse(b, fields);
			return fields;
		}

		@SuppressWarnings("unchecked")
		private void parse(Binding b, List<String> fields) {
			if (b.getValue() != null)
				fields.add((String) b.getValue());
			else {
				for (Binding c : b.getChildren()) {
					if (c.getValue() != null) {
						if (c.getValue() instanceof Collection)
							fields.addAll((List<? extends String>) c.getValue());
						else
							fields.add((String) c.getValue());
					} else
						parse(c, fields);
				}
			}
		}
	}
}
