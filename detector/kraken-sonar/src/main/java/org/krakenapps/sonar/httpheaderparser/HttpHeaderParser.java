package org.krakenapps.sonar.httpheaderparser;

import java.text.ParseException;

import org.krakenapps.bnf.Syntax;
import org.krakenapps.sonar.httpheaderparser.syntax.HttpHeaderSyntax;

public class HttpHeaderParser {
	private Syntax syntax;
	
	public HttpHeaderParser() {
		syntax = HttpHeaderSyntax.create();
	}
	
	public Object eval(String httpheader) throws ParseException {
		return syntax.eval(httpheader);
	}
	
//	public static void main(String[] args) throws ParseException {
//		HttpHeaderParser parser = new HttpHeaderParser();
//		
//		Date begin = new Date();
//		runTests(parser);
//		Date end = new Date();
//		System.out.println((end.getTime() - begin.getTime()) + " milliseconds");
//	}
//	
//	private static void runTests(HttpHeaderParser parser) throws ParseException {
//		test(parser, "User-Agent : 1st/1.0 ((comment in comment)) 2nd 3rd/beta 4th 5th");
//		test(parser, "User-Agent : () (ctext ctext2		ctext3\n\r ctext4\n\r	ctext5) (\\n\\r) (()) ((comment in comment)) (((comment in comment in comment)))");
//	}
//	
//	@SuppressWarnings("unchecked")
//	private static void test(HttpHeaderParser parser, String sql) throws ParseException {
//		for( HttpApplicationMetaData result : (List<HttpApplicationMetaData>)parser.eval(sql) )
//		{
//			System.out.println(result.toString());
//		}
//	}
}
