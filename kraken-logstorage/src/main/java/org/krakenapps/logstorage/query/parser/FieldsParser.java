package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.StringPlaceholder;
import org.krakenapps.logstorage.query.command.Fields;

public class FieldsParser implements QueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("fields", this, k("fields"), repeat(new StringPlaceholder(new char[] { ' ', ',' })));
		syntax.addRoot("fields");
	}

	@Override
	public Object parse(Binding b) {
		boolean remove = false;
		List<String> fields = new ArrayList<String>();

		Binding c = b.getChildren()[1];

		if (c.getValue() != null) {
			if (c.getValue().equals("-"))
				remove = true;
			if (c.getChildren() != null) {
				for (int i = 0; i < c.getChildren().length; i++)
					parse(c.getChildren()[i], fields);
			}
		} else {
			if (c.getChildren() != null) {
				int i = 0;
				if (c.getChildren()[0].getValue().equals("-")) {
					remove = true;
					i = 1;
				}

				for (; i < c.getChildren().length; i++)
					parse(c.getChildren()[i], fields);
			}
		}

		return new Fields(remove, fields);
	}

	private void parse(Binding b, List<String> fields) {
		if (b.getValue() != null)
			fields.add((String) b.getValue());

		if (b.getChildren() != null) {
			for (int i = 0; i < b.getChildren().length; i++)
				parse(b.getChildren()[i], fields);
		}
	}
}
