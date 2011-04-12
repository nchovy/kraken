/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.sqlparser.parser;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.sqlparser.ast.ReferencesSpecification;

public class ReferencedTableAndColumnsParser implements Parser {

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Binding b) {
		Binding[] children = b.getChildren();
		String tableName = (String) children[0].getValue();

		List<String> columns = new ArrayList<String>();
		if (children.length > 1) {
			// columns = ( list )
			Binding[] columnBindings = children[1].getChildren();
			columns = (List<String>) columnBindings[1].getValue();
		}

		return new ReferencesSpecification(tableName, columns);
	}

}
