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
package org.krakenapps.bnf;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.krakenapps.bnf.Syntax.*;
import static org.junit.Assert.*;

public class ColumnListParserTest {
	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		try {
			Syntax s = new Syntax();
			s.add("column_name_list", new ColumnNameListParser(), t("("), option(ref("column_name"), repeat(rule(
					t(","), ref("column_name")))), t(")"));
			s.add("column_name", null, idvar());

			s.addRoot("column_name_list");

			// general case
			Object ret = s.eval("(a, b, c, d)");
			assertNotNull(ret);

			List<String> names = (List<String>) ret;
			assertEquals("a", names.get(0));
			assertEquals("b", names.get(1));
			assertEquals("c", names.get(2));
			assertEquals("d", names.get(3));

			// empty test
			names = (List<String>) s.eval("()");
			assertEquals(0, names.size());

			// one element test
			names = (List<String>) s.eval("(a)");
			assertEquals(1, names.size());
			assertEquals("a", names.get(0));

			// invalid syntax
			try {
				s.eval("(a,)");
				assertTrue(false);
			} catch (Exception e) {
			}
		} catch (Exception e) {
			assertFalse(true);
		}
	}

	private static class ColumnNameListParser implements Parser {
		@Override
		public Object parse(Binding b) {
			List<String> names = new ArrayList<String>();
			ParserUtil.buildList(b, names);
			return names;
		}
	}
}
