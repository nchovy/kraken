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
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.query.StringPlaceholder;
import org.krakenapps.logdb.query.command.Rename;

public class RenameParser implements LogQueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("rename", this, k("rename "), new StringPlaceholder(), k("as "), new StringPlaceholder());
		syntax.addRoot("rename");
	}

	@Override
	public Object parse(Binding b) {
		String from = (String) b.getChildren()[1].getValue();
		String to = (String) b.getChildren()[3].getValue();
		return new Rename(from, to);
	}
}
