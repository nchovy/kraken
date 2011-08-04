package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.StringPlaceholder;
import org.krakenapps.logstorage.query.command.Function;
import org.krakenapps.logstorage.query.command.Stats;

public class StatsParser implements QueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("stats", new StatsParser(), k("stats"), option(k("allnum"), t("="), choice(k("true"), k("false"))),
				option(k("delim"), t("="), choice(k("true"), k("false"))), ref("function"),
				option(k("by"), ref("stats_field")));
		syntax.add("stats_field", new StatsParser.StatsFieldParser(), new StringPlaceholder(','),
				option(ref("stats_field")));
		syntax.addRoot("stats");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Binding b) {
		List<String> keyFields = new ArrayList<String>();
		Function[] func = null;
		for (int i = 2; i < b.getChildren().length; i++) {
			Object v = b.getChildren()[i].getValue();
			Binding[] c = b.getChildren()[i].getChildren();

			if (v instanceof List)
				func = ((List<Function>) v).toArray(new Function[0]);
			else if (v == null && c[0].getValue().equals("by"))
				keyFields = (List<String>) c[1].getValue();
		}

		return new Stats(keyFields, func);
	}

	public static class StatsFieldParser implements Parser {
		@Override
		public Object parse(Binding b) {
			List<String> fields = new ArrayList<String>();

			parse(b, fields);

			return fields;
		}

		@SuppressWarnings("unchecked")
		private void parse(Binding b, List<String> fields) {
			if (b.getValue() != null)
				fields.add((String) b.getValue());
			else {
				for (Binding c : b.getChildren()) {
					if (c.getValue() != null) {
						if (c.getValue() instanceof Collection)
							fields.addAll((List<? extends String>) c.getValue());
						else
							fields.add((String) c.getValue());
					} else
						parse(c, fields);
				}
			}
		}
	}
}
