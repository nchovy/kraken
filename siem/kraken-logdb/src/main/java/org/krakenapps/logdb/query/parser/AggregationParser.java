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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.query.aggregator.AggregationField;
import org.krakenapps.logdb.query.aggregator.AggregationFunction;
import org.krakenapps.logdb.query.aggregator.Average;
import org.krakenapps.logdb.query.aggregator.Count;
import org.krakenapps.logdb.query.aggregator.First;
import org.krakenapps.logdb.query.aggregator.Last;
import org.krakenapps.logdb.query.aggregator.Max;
import org.krakenapps.logdb.query.aggregator.Min;
import org.krakenapps.logdb.query.aggregator.PerDay;
import org.krakenapps.logdb.query.aggregator.PerHour;
import org.krakenapps.logdb.query.aggregator.PerMinute;
import org.krakenapps.logdb.query.aggregator.PerSecond;
import org.krakenapps.logdb.query.aggregator.Range;
import org.krakenapps.logdb.query.aggregator.Sum;
import org.krakenapps.logdb.query.expr.Expression;

public class AggregationParser {
	private static final String AS = " as ";
	private static Map<String, Class<? extends AggregationFunction>> t;

	static {
		t = new HashMap<String, Class<? extends AggregationFunction>>();
		t.put("c", Count.class);
		t.put("count", Count.class);
		t.put("sum", Sum.class);
		t.put("avg", Average.class);
		t.put("first", First.class);
		t.put("last", Last.class);
		t.put("max", Max.class);
		t.put("min", Min.class);
		t.put("per_day", PerDay.class);
		t.put("per_hour", PerHour.class);
		t.put("per_minute", PerMinute.class);
		t.put("per_second", PerSecond.class);
		t.put("range", Range.class);
	}

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
		Class<?> c = t.get(funcName);
		if (c == null)
			throw new LogQueryParseException("invalid-aggregation-function", -1, "function name token is [" + funcName + "]");

		try {
			return (AggregationFunction) c.getConstructors()[0].newInstance(exprs);
		} catch (Throwable e) {
			throw new LogQueryParseException("cannot-create-aggregation-function", -1, e.getMessage());
		}
	}
}
