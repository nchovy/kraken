/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
