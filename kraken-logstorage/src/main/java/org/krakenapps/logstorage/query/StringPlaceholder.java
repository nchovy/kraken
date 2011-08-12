package org.krakenapps.logstorage.query;

import java.nio.BufferUnderflowException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.ParserContext;
import org.krakenapps.bnf.Placeholder;
import org.krakenapps.bnf.Result;
import org.krakenapps.bnf.StringUtil;

public class StringPlaceholder implements Placeholder {
	private Set<Character> end = new HashSet<Character>();

	public StringPlaceholder() {
		this(' ');
	}

	public StringPlaceholder(char end) {
		this.end.add(end);
	}

	public StringPlaceholder(char[] end) {
		for (char c : end)
			this.end.add(c);
	}

	@Override
	public Result eval(String text, int position, ParserContext ctx) throws ParseException {
		StringBuilder sb = new StringBuilder();
		int i = StringUtil.skipSpaces(text, position);

		int begin = i;

		if (text.length() <= begin)
			throw new BufferUnderflowException();

		i = findEnd(sb, text, i);

		String token = sb.toString();

		// remove trailing spaces
		i = StringUtil.skipSpaces(text, i);
		return new Result(new Binding(this, token), i);
	}

	private int findEnd(StringBuilder sb, String text, int position) throws ParseException {
		int i = position;
		boolean quote = false;
		StringBuilder q = null;

		while (i < text.length()) {
			char c = text.charAt(i++);

			if (quote) {
				if (c == '"') {
					quote = !quote;
					sb.append(q.toString().replace("\\\\", "\\").replace("\\\"", "\""));
				} else if (c == '\\') {
					q.append(c);
					q.append(text.charAt(i++));
				} else
					q.append(c);
			} else {
				if (end.contains(c))
					break;
				else if (c == '"') {
					quote = !quote;
					q = new StringBuilder();
				} else
					sb.append(c);
			}
		}

		if (quote)
			throw new ParseException("not properly closed by a double-quote", i);

		return i;
	}
}
