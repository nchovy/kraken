package org.krakenapps.sqlparser.parser;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.sqlparser.ast.AlterColumnAction;
import org.krakenapps.sqlparser.ast.AlterColumnDefinition;

public class AlterColumnDefinitionParser implements Parser {
	@Override
	public Object parse(Binding b) {
		AlterColumnAction action = (AlterColumnAction) b.getChildren()[2].getValue();
		return new AlterColumnDefinition(action);
	}
}
