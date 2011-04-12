package org.krakenapps.sonar.httpheaderparser.parser;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;

public class QuotedPairParser implements Parser {
	
	@Override
	public Object parse(Binding b) {
		Binding[] children = b.getChildren();
		
		if (children == null) {
			return b.getValue();
		}
		
		if( children != null && children.length == 2 ){
			return "\\" + children[1].getValue();
		}
		
		throw new UnsupportedOperationException();
	}
}
