package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import java.util.List;
import java.util.Map;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.FunctionPlaceholder;
import org.krakenapps.logstorage.query.StringPlaceholder;
import org.krakenapps.logstorage.query.command.Function;
import org.krakenapps.logstorage.query.command.Timechart;
import org.krakenapps.logstorage.query.command.Timechart.Span;

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
