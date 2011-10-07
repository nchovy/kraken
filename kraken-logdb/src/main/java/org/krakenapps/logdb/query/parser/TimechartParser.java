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

import java.util.List;
import java.util.Map;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.query.FunctionPlaceholder;
import org.krakenapps.logdb.query.StringPlaceholder;
import org.krakenapps.logdb.query.command.Function;
import org.krakenapps.logdb.query.command.Timechart;
import org.krakenapps.logdb.query.command.Timechart.Span;
import org.krakenapps.logdb.query.parser.FunctionParser;
import org.krakenapps.logdb.query.parser.QueryParser;
import org.krakenapps.logdb.query.parser.TimechartParser;

public class TimechartParser implements QueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("timechart", new TimechartParser(), k("timechart"), ref("option"), ref("timechart_function"),
				k("by"), new StringPlaceholder());
		syntax.add("timechart_function", new FunctionParser(), new FunctionPlaceholder(Timechart.func),
				option(k("as"), new StringPlaceholder(new char[] { ' ', ',' })), option(ref("timechart_function")));
		syntax.addRoot("timechart");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Binding b) {
		Function[] func = ((List<Function>) b.getChildren()[2].getValue()).toArray(new Function[0]);
		String clause = (String) b.getChildren()[4].getValue();
		Map<String, String> option = (Map<String, String>) b.getChildren()[1].getValue();
		Span field = null;
		Integer amount = null;

		if (option.containsKey("span")) {
			String value = option.get("span");
			int i;
			for (i = 0; i < value.length(); i++) {
				char c = value.charAt(i);
				if (!('0' <= c && c <= '9'))
					break;
			}
			String f = value.substring(i);
			if (f.equalsIgnoreCase("s"))
				field = Span.Second;
			else if (f.equalsIgnoreCase("m"))
				field = Span.Minute;
			else if (f.equalsIgnoreCase("h"))
				field = Span.Hour;
			else if (f.equalsIgnoreCase("d"))
				field = Span.Day;
			else if (f.equalsIgnoreCase("w"))
				field = Span.Week;
			else if (f.equalsIgnoreCase("mon"))
				field = Span.Month;
			else if (f.equalsIgnoreCase("y"))
				field = Span.Year;
			amount = Integer.parseInt(value.substring(0, i));
		}

		if (field == null)
			return new Timechart(func, clause);
		else
			return new Timechart(field, amount, func, clause);
	}
}
