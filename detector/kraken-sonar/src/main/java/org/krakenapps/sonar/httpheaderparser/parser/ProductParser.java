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
import org.krakenapps.sonar.passive.fingerprint.HttpApplicationMetaData;

public class ProductParser implements Parser {
	
	@Override
	public Object parse(Binding b) {
		Binding[] children = b.getChildren();
		
		if (children == null) {
			return new HttpApplicationMetaData("vendor", (String)b.getValue(), "version");
			//return "Product Name : " + b.getValue();
		}
		
		if( children != null && children.length == 2 ){
			//String strRet = "Product Name : " + children[0].getValue(); 
			
			if( children[1].getChildren() != null && children[1].getChildren().length == 2 )
			{
				//strRet += " (Version : " + children[1].getChildren()[1].getValue() + ")";
				
				return new HttpApplicationMetaData("vendor", (String)children[0].getValue(), 
						(String)children[1].getChildren()[1].getValue());
			}
			
			return new HttpApplicationMetaData("vendor", (String)children[0].getValue(),
					"version");
			//return strRet;
		}
		
		throw new UnsupportedOperationException();
	}
}
