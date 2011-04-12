package org.krakenapps.sonar.httpheaderparser.syntax;

import java.text.ParseException;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.ParserContext;
import org.krakenapps.bnf.Result;
import org.krakenapps.bnf.Rule;
import org.krakenapps.bnf.StringUtil;

public class Token implements Rule {
	
	private boolean isCTL(char c)
	{
		if( 0<= (int)c && (int)c <= 31 )
			return true;
		
		if( (int)c == 127 )
			return true;
			
		return false;
	}
	
	private boolean isSeparator(char c)
	{
		if( c=='('||c==')'||c=='<'||c=='>'||c=='@'||c==','||c==';'||c==':'||c=='\\'
			||c=='/'||c=='['||c==']'||c=='?'||c=='='||c=='{'||c=='}'||c==' '||c=='\t' )
			return true;
		
		return false;
	}

	@Override
	public Result eval(String text, int position, ParserContext ctx) throws ParseException {
		position = StringUtil.skipSpaces(text, position);
		
		if( position >= text.length() )
			throw new ParseException("end-of-text reached", position);
		
		String token = "";
		int i;
		for( i=0 ; position + i < text.length() ; i++ ) 
		{
			if( isSeparator(text.charAt(position+i)) || isCTL(text.charAt(position+i)) )
			{
				break;
			}
			else
			{
				token = token + text.charAt(position+i);
			}
		}
		
		if( i < 1 )
			throw new ParseException("cannot be a token", position);
		else
		{
			//System.out.println("TOKEN:"+token);
			return new Result(new Binding(this, token), position+token.length());
		}
	}

}
