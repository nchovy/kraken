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
import org.krakenapps.sqlparser.ast.ColumnConstraintDefinition;
import org.krakenapps.sqlparser.ast.ColumnDefinition;
import org.krakenapps.sqlparser.ast.DataType;

public class ColumnDefinitionParser implements Parser {

	@Override
	public Object parse(Binding b) {
		Binding[] children = b.getChildren();
		String columnName = (String) children[0].getValue();
		DataType dataType = (DataType) children[1].getValue();

		List<ColumnConstraintDefinition> constraints = new ArrayList<ColumnConstraintDefinition>();

		if (children.length > 2) {
			for (int i = 2; i < children.length; i++) {
				parse(children[i], constraints);
			}
		}

		ColumnDefinition def = new ColumnDefinition(columnName, dataType, constraints);
		return def;
	}

	private void parse(Binding b, List<ColumnConstraintDefinition> constraints) {
		if (b.getChildren() == null) {
			constraints.add((ColumnConstraintDefinition) b.getValue());
			return;
		}
		
		for (int i = 0; i < b.getChildren().length; i++) {
			Binding child = b.getChildren()[i];
			if (child.getValue() instanceof ColumnConstraintDefinition) {
				constraints.add((ColumnConstraintDefinition) child.getValue());
			}
		}
	}

}
