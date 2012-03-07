package org.krakenapps.sonar.httpheaderparser.syntax;

import java.text.ParseException;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.ParserContext;
import org.krakenapps.bnf.Result;
import org.krakenapps.bnf.Rule;

public class Char implements Rule {
	
	@Override
	public Result eval(String text, int position, ParserContext ctx) throws ParseException {
		if( position >= text.length() )
			throw new ParseException("end-of-text reached", position);
		
		// CHAR : any value between 0 and 127
		if( 0 <= (int)text.charAt(position) && (int)text.charAt(position) <= 127 )
		{
			//System.out.println("CHAR:"+text.charAt(position));
			return new Result(new Binding(this, text.charAt(position)), position+1);
		}
		
		throw new ParseException("not a CHAR", position);
	}
	
}
