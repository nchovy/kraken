/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.console;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author xeraph
 * 
 */
public class ScriptArgumentParserTest {
	@Test
	public void testParse() {
		String haystack0 = "abc";
		System.out.println("test0: " + haystack0);
		String[] tokenized = ScriptArgumentParser.tokenize(haystack0);
		assertEquals("abc", tokenized[0]);

		String haystack1 = "abc def ghi";
		System.out.println("test1: " + haystack1);
		tokenized = ScriptArgumentParser.tokenize(haystack1);
		assertEquals("abc", tokenized[0]);
		assertEquals("def", tokenized[1]);
		assertEquals("ghi", tokenized[2]);

		String haystack2 = "\"abc \\\"hello\\\" def\" \"hah aha\" \"abc \\\\\\\" def\"";
		System.out.println("test2: " + haystack2);
		tokenized = ScriptArgumentParser.tokenize(haystack2);

		assertEquals("abc \"hello\" def", tokenized[0]);
		assertEquals("hah aha", tokenized[1]);
		assertEquals("abc \\\" def", tokenized[2]);

		String haystack3 = "run \"c:\\Program Files\\Kraken\"";
		System.out.println("test3: " + haystack3);
		tokenized = ScriptArgumentParser.tokenize(haystack3);

		assertEquals("run", tokenized[0]);
		assertEquals("c:\\Program Files\\Kraken", tokenized[1]);

		String haystack4 = "   \"  space world   \" \t";
		System.out.println("test4: " + haystack4);
		tokenized = ScriptArgumentParser.tokenize(haystack4);
		
		assertEquals(1, tokenized.length);
		assertEquals("  space world   ", tokenized[0]);
		
		String haystack5 = "\" \"";
		System.out.println("test5: " + haystack5);
		tokenized = ScriptArgumentParser.tokenize(haystack5);
		
		assertEquals(1, tokenized.length);
		assertEquals(" ", tokenized[0]);
		
	}
}