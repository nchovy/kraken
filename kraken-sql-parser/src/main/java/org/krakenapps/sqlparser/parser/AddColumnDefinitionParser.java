package org.krakenapps.sqlparser.parser;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.sqlparser.ast.AddColumnDefinition;
import org.krakenapps.sqlparser.ast.ColumnDefinition;

public class AddColumnDefinitionParser implements Parser {

	@Override
	public Object parse(Binding b) {
		int size = b.getChildren().length;
		return new AddColumnDefinition((ColumnDefinition) b.getChildren()[size - 1].getValue());
	}

}
