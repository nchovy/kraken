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

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.ParserContext;
import org.krakenapps.bnf.Placeholder;
import org.krakenapps.bnf.Result;
import org.krakenapps.bnf.StringUtil;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQueryParser;

public class OptionParser implements LogQueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("option", this, repeat(new OptionPlaceholder()));
	}

	@Override
	public Object parse(Binding b) {
		Map<String, String> option = new HashMap<String, String>();
		parse(option, b);
		return option;
	}

	private void parse(Map<String, String> m, Binding b) {
		if (b == null)
			return;

		if (b.getValue() != null)
			put(m, (String) b.getValue());
		if (b.getChildren() != null) {
			for (Binding c : b.getChildren())
				parse(m, c);
		}
	}

	private void put(Map<String, String> m, String opt) {
		int index = opt.indexOf("=");
		m.put(opt.substring(0, index), opt.substring(index + 1));
	}

	private class OptionPlaceholder implements Placeholder {
		@Override
		public Result eval(String text, int position, ParserContext ctx) throws ParseException {
			int begin = StringUtil.skipSpaces(text, position);
			int end = text.indexOf(" ", begin);

			if (end == -1)
				end = text.length();
			String option = text.substring(begin, end);
			if (!option.contains("="))
				throw new ParseException("not option", begin);
			if (option.startsWith("("))
				throw new ParseException("not option", begin);

			return new Result(new Binding(this, option), StringUtil.skipSpaces(text, end));
		}
	}
}
