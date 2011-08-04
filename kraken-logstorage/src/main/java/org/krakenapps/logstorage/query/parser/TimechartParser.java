package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import java.util.List;

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
		syntax.add("timechart", new TimechartParser(), k("timechart"),
				option(k("span"), k("="), uint(), choice(k("s"), k("m"), k("h"), k("d"), k("w"))),
				ref("timechart_function"), k("by"), new StringPlaceholder());
		syntax.add("timechart_function", new FunctionParser(), new FunctionPlaceholder(Timechart.func),
				option(k("as"), new StringPlaceholder()), option(t(",")), option(ref("timechart_function")));
		syntax.addRoot("timechart");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Binding b) {
		Function[] func = null;
		String clause = (String) b.getChildren()[b.getChildren().length - 1].getValue();
		Span field = null;
		Integer amount = null;

		for (int i = 1; i < b.getChildren().length - 2; i++) {
			Object v = b.getChildren()[i].getValue();
			Binding[] c = b.getChildren()[i].getChildren();

			// TODO allnum, delim
			if (v instanceof List)
				func = ((List<Function>) v).toArray(new Function[0]);
			else if (v == null && c[0].getValue().equals("span")) {
				amount = (Integer) c[c.length - 2].getValue();
				String f = (String) c[c.length - 1].getValue();
				if (f.equals("s"))
					field = Span.Second;
				else if (f.equals("m"))
					field = Span.Minute;
				else if (f.equals("h"))
					field = Span.Hour;
				else if (f.equals("d"))
					field = Span.Day;
				else if (f.equals("w"))
					field = Span.Week;
			} else if (v == null && c[0].getValue().equals("by"))
				clause = (String) c[1].getValue();
		}

		if (field == null)
			return new Timechart(func, clause);
		else
			return new Timechart(field, amount, func, clause);
	}
}
