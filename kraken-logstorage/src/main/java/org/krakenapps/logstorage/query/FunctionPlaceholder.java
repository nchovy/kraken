package org.krakenapps.logstorage.query;

import java.nio.BufferUnderflowException;
import java.text.ParseException;
import java.util.Map;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.ParserContext;
import org.krakenapps.bnf.Placeholder;
import org.krakenapps.bnf.Result;
import org.krakenapps.bnf.StringUtil;
import org.krakenapps.logstorage.query.command.Function;

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
