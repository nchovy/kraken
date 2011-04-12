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
import org.krakenapps.sqlparser.ast.IntegerDataType;

/**
 * exact_numeric_type ::= NUMERIC ( precision, scale ) | DECIMAL ( precision,
 * scale ) | DEC ( precision, scale ) | INTEGER | INT | SMALLINT
 * 
 * @author xeraph
 * 
 */
public class ExactNumericTypeParser implements Parser {

	@Override
	public Object parse(Binding b) {
		String dataTypeName = (String) b.getValue();

		if (dataTypeName.equalsIgnoreCase("INTEGER") || dataTypeName.equalsIgnoreCase("INT"))
			return new IntegerDataType();

		return new UnsupportedOperationException(dataTypeName + " not supported");
	}

}
