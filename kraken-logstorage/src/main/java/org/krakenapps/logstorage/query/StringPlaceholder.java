package org.krakenapps.logstorage.query;

import java.nio.BufferUnderflowException;
import java.text.ParseException;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.ParserContext;
import org.krakenapps.bnf.Placeholder;
import org.krakenapps.bnf.Result;
import org.krakenapps.bnf.StringUtil;

public class StringPlaceholder implements Placeholder {
	private StringBuilder sb;
	private Character end;

	public StringPlaceholder() {
	}

	public StringPlaceholder(char end) {
		this.end = end;
	}

	@Override
	public Result eval(String text, int position, ParserContext ctx) throws ParseException {
		sb = new StringBuilder();
		int i = StringUtil.skipSpaces(text, position);

		int begin = i;

		if (text.length() <= begin)
			throw new BufferUnderflowException();

		i = findEnd(text, i);

		String token = sb.toString();

		if (end != null && i < text.length() && text.charAt(i) == end.charValue())
			i++;

		// remove trailing spaces
		i = StringUtil.skipSpaces(text, i);
		return new Result(new Binding(this, token), i);
	}

	private int findEnd(String text, int position) throws ParseException {
		int i = position;
		boolean quote = false;
		StringBuilder q = new StringBuilder();

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
				if (c == ' ')
					break;
				else if (end != null && c == end.charValue())
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
