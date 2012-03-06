package org.krakenapps.sonar.httpheaderparser.parser;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;

public class CommentParser implements Parser {
	
	private String extractValueFromBinding( Binding b )
	{
		Binding[] children = b.getChildren();
		
		if( children == null )
			return (String) b.getValue();
		
		String strRet="";
		for( Binding child : children )
		{
			strRet += extractValueFromBinding(child);
		}
		
		return strRet;
	}
	
	@Override
	public Object parse(Binding b) {
		// do not use comment data
		return null;
		
//		Binding[] children = b.getChildren();
//		
//		if( children == null )
//		{
//			return b.getValue();
//		}
//		
//		if (children.length == 2) {
//			return "<(Empty)>";
//		}
//		
//		if( children.length == 3 ) {
//			return "<" + extractValueFromBinding(children[1]) + ">";
//		}
//		
//		throw new UnsupportedOperationException();
	}
}
