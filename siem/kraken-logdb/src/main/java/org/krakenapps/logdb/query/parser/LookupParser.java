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

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LookupHandlerRegistry;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.query.StringPlaceholder;
import org.krakenapps.logdb.query.command.Lookup;

public class LookupParser implements LogQueryParser {
	private LookupHandlerRegistry registry;

	public LookupParser(LookupHandlerRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("lookup", this, k("lookup "), ref("option"), new StringPlaceholder(), ref("lookup_field"),
				k("OUTPUT"), ref("lookup_field"));
		syntax.add("lookup_field", new LookupFieldParser(), new StringPlaceholder(),
				option(k("as "), new StringPlaceholder()));
		syntax.addRoot("lookup");
	}

	@Override
	public Object parse(Binding b) {
		String handlerName = (String) b.getChildren()[2].getValue();
		LookupField src = (LookupField) b.getChildren()[3].getValue();
		LookupField dst = (LookupField) b.getChildren()[5].getValue();
		Lookup lookup = new Lookup(handlerName, src.first, src.second, dst.first, dst.second);
		lookup.setLogQueryService(registry);
		return lookup;
	}

	private class LookupField {
		private String first;
		private String second;
	}

	public class LookupFieldParser implements Parser {
		@Override
		public Object parse(Binding b) {
			LookupField field = new LookupField();
			if (b.getValue() != null) {
				field.first = (String) b.getValue();
				field.second = (String) b.getValue();
			} else {
				field.first = (String) b.getChildren()[0].getValue();
				field.second = (String) b.getChildren()[1].getChildren()[1].getValue();
			}
			return field;
		}
	}
}
