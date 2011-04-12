package org.krakenapps.sonar.passive.ids.rule;

import java.text.ParseException;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.ParserContext;
import org.krakenapps.bnf.Placeholder;
import org.krakenapps.bnf.Result;
import org.krakenapps.bnf.StringUtil;

public class OptionNamePlaceholder implements Placeholder {

	@Override
	public Result eval(String text, int position, ParserContext ctx) throws ParseException {
		int i = StringUtil.skipSpaces(text, position);
		int begin = i;

		while (i < text.length() && text.charAt(i) != ':')
			i++;

		String token = text.substring(begin, i);

		return new Result(new Binding(this, token), i);
	}

}
