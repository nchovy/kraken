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

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.bnf.SequenceRule;
import org.krakenapps.sqlparser.ast.NotNullConstraint;
import org.krakenapps.sqlparser.ast.PrimaryKeyConstraint;
import org.krakenapps.sqlparser.ast.ReferencesSpecification;
import org.krakenapps.sqlparser.ast.UniqueConstraint;
import org.krakenapps.sqlparser.ast.UniqueSpecification;

public class ColumnConstraintParser implements Parser {

	@Override
	public Object parse(Binding b) {
		if (b.getRule() instanceof SequenceRule && b.getChildren().length == 2) {
			String first = (String) b.getChildren()[0].getValue();
			String second = (String) b.getChildren()[1].getValue();
			if (first.equalsIgnoreCase("NOT") && second.equalsIgnoreCase("NULL"))
				return new NotNullConstraint();
		}

		if (b.getValue() instanceof UniqueSpecification) {
			UniqueSpecification spec = (UniqueSpecification) b.getValue();
			if (spec.equals(UniqueSpecification.Unique))
				return new UniqueConstraint();
			else if (spec.equals(UniqueSpecification.PrimaryKey))
				return new PrimaryKeyConstraint();
		}
		
		if (b.getValue() instanceof ReferencesSpecification)
			return b.getValue();

		throw new IllegalStateException("invalid column constraint: " + b.getValue());
	}

}
