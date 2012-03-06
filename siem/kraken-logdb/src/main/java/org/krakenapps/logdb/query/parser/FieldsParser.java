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
import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.query.StringPlaceholder;
import org.krakenapps.logdb.query.command.Fields;

public class FieldsParser implements LogQueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("fields", this, k("fields "), repeat(new StringPlaceholder(new char[] { ' ', ',' })));
		syntax.addRoot("fields");
	}

	@Override
	public Object parse(Binding b) {
		boolean remove = false;
		List<String> fields = new ArrayList<String>();

		Binding c = b.getChildren()[1];

		if (c.getValue() != null) {
			if (c.getValue().equals("-"))
				remove = true;
			else
				fields.add((String) c.getValue());

			if (c.getChildren() != null) {
				for (int i = 0; i < c.getChildren().length; i++)
					parse(c.getChildren()[i], fields);
			}
		} else {
			if (c.getChildren() != null) {
				int i = 0;
				if (c.getChildren()[0].getValue().equals("-")) {
					remove = true;
					i = 1;
				}

				for (; i < c.getChildren().length; i++)
					parse(c.getChildren()[i], fields);
			}
		}

		return new Fields(remove, fields);
	}

	private void parse(Binding b, List<String> fields) {
		if (b.getValue() != null)
			fields.add((String) b.getValue());

		if (b.getChildren() != null) {
			for (int i = 0; i < b.getChildren().length; i++)
				parse(b.getChildren()[i], fields);
		}
	}
}
