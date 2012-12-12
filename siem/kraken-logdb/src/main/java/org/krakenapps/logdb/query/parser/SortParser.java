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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.query.StringPlaceholder;
import org.krakenapps.logdb.query.command.Sort;
import org.krakenapps.logdb.query.command.Sort.SortField;
import org.krakenapps.logdb.query.parser.SortParser;

public class SortParser implements LogQueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("sort2", this, k("sort2 "), ref("option"), ref("sort_field2"));
		syntax.add("sort_field2", new SortParser.SortFieldParser(),
				repeat(rule(option(choice(t("+"), t("-"))), new StringPlaceholder(new char[] { ' ', ',' }))));
		syntax.addRoot("sort2");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Binding b) {
		Map<String, String> option = (Map<String, String>) b.getChildren()[1].getValue();
		Integer count = null;
		SortField[] fields = (SortField[]) b.getChildren()[2].getValue();

		if (option.containsKey("limit"))
			count = Integer.parseInt(option.get("limit"));

		try {
			return new Sort(count, fields);
		} catch (IOException e) {
			return null;
		}
	}

	public static class SortFieldParser implements Parser {
		@Override
		public Object parse(Binding b) {
			List<SortField> fields = new ArrayList<SortField>();
			parse(b, fields);
			String lastName = fields.get(fields.size() - 1).getName();
			if (lastName.equalsIgnoreCase("d") || lastName.equalsIgnoreCase("desc")) {
				fields.remove(fields.size() - 1);
				for (SortField field : fields)
					field.reverseAsc();
			}

			return fields.toArray(new SortField[0]);
		}

		private void parse(Binding b, List<SortField> fields) {
			if (b.getValue() != null)
				fields.add(new SortField((String) b.getValue()));
			else {
				if (b.getChildren().length == 1) {
					if (b.getChildren()[0].getValue() != null)
						fields.add(new SortField((String) b.getChildren()[0].getValue()));
				} else if (b.getChildren().length == 2) {
					String v1 = (String) b.getChildren()[0].getValue();
					String v2 = (String) b.getChildren()[1].getValue();

					if (v1 != null && v2 != null) {
						if (v1.equals("-") || v1.equals("+"))
							fields.add(new SortField(v2, !v1.equals("-")));
						else {
							fields.add(new SortField(v1));
							fields.add(new SortField(v2));
						}
					} else if (v1 != null && v2 == null) {
						fields.add(new SortField(v1));
						parse(b.getChildren()[1], fields);
					} else if (v1 == null && v2 != null) {
						parse(b.getChildren()[0], fields);
						fields.add(new SortField(v2));
					} else {
						parse(b.getChildren()[0], fields);
						parse(b.getChildren()[1], fields);
					}
				}
			}
		}
	}
}
