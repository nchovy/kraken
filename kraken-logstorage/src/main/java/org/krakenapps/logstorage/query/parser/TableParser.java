package org.krakenapps.logstorage.query.parser;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.logstorage.query.command.Table;

public class TableParser implements Parser {
	@Override
	public Object parse(Binding b) {
		String tableName = (String) b.getChildren()[1].getValue();

		if (b.getChildren().length == 3)
			return new Table(tableName, (Integer) b.getChildren()[2].getValue());

		return new Table(tableName);
	}
}
