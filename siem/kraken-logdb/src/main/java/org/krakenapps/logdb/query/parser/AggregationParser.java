/*
 * Copyright 2013 Future Systems
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

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.query.aggregator.AggregationField;
import org.krakenapps.logdb.query.aggregator.AggregationFunction;
import org.krakenapps.logdb.query.aggregator.Count;
import org.krakenapps.logdb.query.aggregator.Sum;
import org.krakenapps.logdb.query.expr.Expression;

public class AggregationParser {
	private static final String AS = " as ";

	public static AggregationField parse(String s) {
		// find 'as' keyword
		String funcPart = s.trim();
		String alias = null;
		int p = QueryTokenizer.findKeyword(s, AS);
		if (p > 0) {
			funcPart = s.substring(0, p).trim();
			alias = s.substring(p + AS.length());
		}

		// find aggregation function
		AggregationFunction func = parseFunc(funcPart);

		// build and return
		AggregationField field = new AggregationField();
		field.setName(alias != null ? alias : func.getName());
		field.setFunction(func);
		return field;
	}

	private static AggregationFunction parseFunc(String s) {
		int p = s.indexOf('(');
		String funcName = s;
		String argsToken = "";
		if (p > 0) {
			funcName = s.substring(0, p);

			// TODO: check closing parens
			argsToken = s.substring(p + 1, s.length() - 1);
		}

		List<String> argTokens = QueryTokenizer.parseByComma(argsToken);
		List<Expression> exprs = new ArrayList<Expression>();

		for (String argToken : argTokens) {
			Expression expr = ExpressionParser.parse(argToken);
			exprs.add(expr);
		}

		// find function
		if (funcName.equals("c") || funcName.equals("count"))
			return new Count(exprs);
		else if (funcName.equals("sum"))
			return new Sum(exprs);

		throw new LogQueryParseException("invalid-aggregation-function", -1, "function name token is [" + funcName + "]");
	}
}
