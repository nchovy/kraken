package org.krakenapps.logstorage.query.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.logstorage.query.command.Function;
import org.krakenapps.logstorage.query.command.Stats;

public class StatsParser implements Parser {
	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Binding b) {
		List<String> keyFields = new ArrayList<String>();
		Function[] func = null;
		for (int i = 1; i < b.getChildren().length; i++) {
			Object v = b.getChildren()[i].getValue();
			Binding[] c = b.getChildren()[i].getChildren();

			// TODO allnum, delim
			if (v instanceof List)
				func = ((List<Function>) v).toArray(new Function[0]);
			else if (v == null && c[0].getValue().equals("by"))
				keyFields = (List<String>) c[1].getValue();
		}

		return new Stats(keyFields, func);
	}

	public static class StatsFunctionParser implements Parser {
		@Override
		public Object parse(Binding b) {
			List<Function> fs = new ArrayList<Function>();

			parse(b, fs);

			return fs;
		}

		@SuppressWarnings("unchecked")
		private void parse(Binding b, List<Function> fs) {
			if (b.getValue() != null)
				fs.add((Function) b.getValue());
			else {
				boolean as = false;
				for (Binding c : b.getChildren()) {
					if (c.getValue() != null) {
						if (c.getValue() instanceof Collection)
							fs.addAll((List<? extends Function>) c.getValue());
						else if (c.getValue() instanceof Function) {
							fs.add((Function) c.getValue());
						} else if (c.getValue() instanceof String) {
							if (c.getValue().equals("as"))
								as = true;
							else if (as) {
								fs.get(fs.size() - 1).setKeyName((String) c.getValue());
								as = false;
							}
						}
					} else
						parse(c, fs);
				}
			}
		}
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
