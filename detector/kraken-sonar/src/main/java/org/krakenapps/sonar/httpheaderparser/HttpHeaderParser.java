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
