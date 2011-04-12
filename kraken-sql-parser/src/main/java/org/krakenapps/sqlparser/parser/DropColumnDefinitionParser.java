package org.krakenapps.sqlparser.parser;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Literal;
import org.krakenapps.bnf.Parser;
import org.krakenapps.sqlparser.ast.DropColumnDefinition;

public class DropColumnDefinitionParser implements Parser {

	@Override
	public Object parse(Binding b) {
		Binding columnNameBinding = findColumNameBinding(b.getChildren());
		return new DropColumnDefinition((String) columnNameBinding.getValue());
	}

	private Binding findColumNameBinding(Binding[] children) {
		for (int i = 0; i < children.length; i++)
			if (!(children[i].getRule() instanceof Literal))
				return children[i];

		return null;
	}
}
