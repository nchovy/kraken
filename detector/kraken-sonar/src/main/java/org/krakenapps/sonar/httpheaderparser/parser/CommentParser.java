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
