package org.krakenapps.sonar.httpheaderparser.syntax;

import java.text.ParseException;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.ParserContext;
import org.krakenapps.bnf.Result;
import org.krakenapps.bnf.Rule;

public class CText implements Rule {

	private boolean isCTL(char c)
	{
		if( 0<= (int)c && (int)c <= 31 )
			return true;
		
		if( (int)c == 127 )
			return true;
			
		return false;
	}
	
	private int processLWS(String text, int startpos)
	{
		int posRet = startpos;
		
		if( (int)text.charAt(startpos) == 10 )
		{
			if( (int)text.charAt(startpos+1) == 13 )
				posRet+=2;
			
			else return startpos;
		}
		
		if( text.charAt(posRet) == 32 || text.charAt(posRet) == 9 )
			posRet++;
		else return startpos;
		
		int i;
		for( i=posRet ; i<text.length() ; i++ )
		{
			if( text.charAt(i) != 32 && text.charAt(i) != 9 )
				break;
		}
		
		return i;
	}
	
	@Override
	public Result eval(String text, int position, ParserContext ctx) throws ParseException {
		if( position >= text.length() )
			throw new ParseException("end-of-text reached", position);
		
		String token = "";
		int i;
		for( i=0 ; position + i < text.length() ; i++ ) 
		{
			if( isCTL(text.charAt(position+i)) )
			{
				int position_after_LWS = processLWS(text, position+i);
				
				if( position_after_LWS == position+i )
					break;
				else
				{
					for(int j=position+i ; j<position_after_LWS ; j++)
						token += text.charAt(j);
					i = (position_after_LWS-position)-1;
				}
			}
			else
			{
				if( text.charAt(position+i) == '(' || text.charAt(position+i) == ')' )
					break;
				else
					token = token + text.charAt(position+i);
			}
		}
		
		if( i<1 )
			throw new ParseException("cannot make CText", position);
		else
		{
			//System.out.println("CTEXT:"+token);
			return new Result(new Binding(this, token), position+token.length());
		}
	}

}
