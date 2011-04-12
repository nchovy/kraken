package org.krakenapps.sonar.httpheaderparser.parser;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.sonar.passive.fingerprint.HttpApplicationMetaData;

public class ServerParser implements Parser {
	
	private List<HttpApplicationMetaData> extractValueFromBinding( Binding b )
	{
		Binding[] children = b.getChildren();
		
		if( children == null )
		{
			List<HttpApplicationMetaData> metaDatas = new ArrayList<HttpApplicationMetaData>();
			metaDatas.add((HttpApplicationMetaData)b.getValue());
			
			return metaDatas;
			//return (String) b.getValue()+"\n";
		}
		
		List<HttpApplicationMetaData> metaDatas = new ArrayList<HttpApplicationMetaData>();
		//String strRet="";
		for( Binding child : children )
		{
			for( HttpApplicationMetaData metadata : extractValueFromBinding(child) )
			{
				if( metadata != null )
					metaDatas.add(metadata);
			}
			//strRet += extractValueFromBinding(child);
		}
		
		return metaDatas;
		//return strRet;
	}
	
	@Override
	public Object parse(Binding b) {
		Binding[] children = b.getChildren();
		
		if (children == null || children.length <= 2) {
			return null;
		}
		
		if( children != null && children.length > 2 ){
			return extractValueFromBinding(children[2]);
		}
		
		throw new UnsupportedOperationException();
	}

}
