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
			char f = value.charAt(value.length() - 1);
			if (f == 's')
				field = Span.Second;
			else if (f == 'm')
				field = Span.Minute;
			else if (f == 'h')
				field = Span.Hour;
			else if (f == 'd')
				field = Span.Day;
			else if (f == 'w')
				field = Span.Week;
			amount = Integer.parseInt(value.substring(0, value.length() - 1));
		}

		if (field == null)
			return new Timechart(func, clause);
		else
			return new Timechart(field, amount, func, clause);
	}
}
