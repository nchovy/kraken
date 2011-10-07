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
package org.krakenapps.logdb.query;

import java.nio.BufferUnderflowException;
import java.text.ParseException;
import java.util.Map;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.ParserContext;
import org.krakenapps.bnf.Placeholder;
import org.krakenapps.bnf.Result;
import org.krakenapps.bnf.StringUtil;
import org.krakenapps.logdb.query.command.Function;

public class FunctionPlaceholder implements Placeholder {
	private Map<String, Class<? extends Function>> func;

	public FunctionPlaceholder() {
	}

	public FunctionPlaceholder(Map<String, Class<? extends Function>> func) {
		this.func = func;
	}

	@Override
	public Result eval(String text, int position, ParserContext ctx) throws ParseException {
		int i = StringUtil.skipSpaces(text, position);

		int begin = i;

		if (text.length() <= begin)
			throw new BufferUnderflowException();

		while (true) {
			char c = text.charAt(i);
			if (c == ' ')
				throw new ParseException("invalid function type", i);
			else if (c == '(')
				break;

			if (++i >= text.length())
				throw new ParseException("invalid function type", i);
		}
		String name = text.substring(begin, i++);

		StringBuilder sb = new StringBuilder();
		int quote = 0;
		while (true) {
			char c = text.charAt(i);
			if (c == '(')
				quote++;
			else if (c == ')') {
				quote--;
				if (quote < 0)
					break;
			}

			sb.append(c);

			if (++i >= text.length())
				throw new ParseException("invalid function type", i);
		}
		String target = sb.toString();

		i = StringUtil.skipSpaces(text, ++i);
		if (i < text.length() && text.charAt(i) == ',')
			i++;

		Function f = Function.getFunction(name, target, func);
		if (f == null)
			throw new ParseException("unknown function", begin);

		return new Result(new Binding(this, f), i);
	}
}
