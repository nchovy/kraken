package org.krakenapps.sonar.httpheaderparser.parser;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;

public class TokenParser implements Parser {

	@Override
	public Object parse(Binding b) {
		if (b.getValue() != null) {
			return b.getValue();
		}
		
		throw new UnsupportedOperationException();
	}

}
