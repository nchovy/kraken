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
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.query.FunctionPlaceholder;
import org.krakenapps.logdb.query.StringPlaceholder;
import org.krakenapps.logdb.query.command.Function;

public class FunctionParser implements LogQueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("function", this, new FunctionPlaceholder(), option(k("as "), new StringPlaceholder(new char[] { ' ', ',' })),
				option(ref("function")));
	}

	@Override
	public Object parse(Binding b) {
		List<Function> fs = new ArrayList<Function>();
		parse(b, fs);
		return fs;
	}

	@SuppressWarnings("unchecked")
	private void parse(Binding b, List<Function> fs) {
		if (b.getValue() != null)
			fs.add((Function) b.getValue());
		else {
			for (int i = 0; i < b.getChildren().length; i++) {
				Binding c = b.getChildren()[i];

				if (c.getValue() != null) {
					if (c.getValue() instanceof Collection)
						fs.addAll((List<? extends Function>) c.getValue());
					else if (c.getValue() instanceof Function) {
						fs.add((Function) c.getValue());
					} else if (c.getValue() instanceof String) {
						fs.get(fs.size() - 1).setKeyName((String) b.getChildren()[i + 1].getValue());
						i++;
					}
				} else {
					parse(c, fs);
				}
			}
		}
	}
}
