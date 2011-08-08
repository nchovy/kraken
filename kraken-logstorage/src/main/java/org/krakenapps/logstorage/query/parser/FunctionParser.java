package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.FunctionPlaceholder;
import org.krakenapps.logstorage.query.StringPlaceholder;
import org.krakenapps.logstorage.query.command.Function;

public class FunctionParser implements QueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("function", this, new FunctionPlaceholder(),
				option(k("as"), new StringPlaceholder(new char[] { ' ', ',' })), option(ref("function")));
	}

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
