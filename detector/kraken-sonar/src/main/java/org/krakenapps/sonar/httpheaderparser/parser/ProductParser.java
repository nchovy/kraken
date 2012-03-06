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
