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
import java.util.Collections;
import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.bnf.ParserUtil;
import org.krakenapps.sqlparser.ast.AndExpression;
import org.krakenapps.sqlparser.ast.BooleanExpression;

public class BooleanTermParser implements Parser {
	@Override
	public Object parse(Binding b) {
		List<BooleanExpression> list = new ArrayList<BooleanExpression>();
		ParserUtil.buildList(b, list);
		Collections.reverse(list);
		return fold(list);
	}

	private BooleanExpression fold(List<BooleanExpression> exprs) {
		if (exprs.size() == 1)
			return exprs.remove(0);
		
		BooleanExpression first = exprs.remove(0);
		return new AndExpression(fold(exprs), first);
	}
}
