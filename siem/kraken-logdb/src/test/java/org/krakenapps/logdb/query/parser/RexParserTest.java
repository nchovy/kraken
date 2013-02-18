/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logdb.query.parser;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryCommand.LogMap;
import org.krakenapps.logdb.query.command.Rex;

public class RexParserTest {
	@Test
	public void testRexItself() {
		Pattern p = Pattern.compile("From: (.*) To: (.*)");
		String[] names = new String[] { "from", "to" };

		DummyOutput out = new DummyOutput();
		Rex rex = new Rex("raw", p, names);
		rex.setNextCommand(out);

		LogMap m = new LogMap();
		m.put("raw", "From: Susan To: Bob");

		rex.push(m);

		assertEquals(1, out.list.size());
		LogMap m2 = out.list.get(0);

		assertEquals("From: Susan To: Bob", (String) m2.get("raw"));
		assertEquals("Susan", (String) m2.get("from"));
		assertEquals("Bob", (String) m2.get("to"));
	}

	private static class DummyOutput extends LogQueryCommand {
		private List<LogMap> list = new ArrayList<LogMap>();

		@Override
		public void push(LogMap m) {
			list.add(m);
		}

		@Override
		public boolean isReducer() {
			return false;
		}
	}

	@Test
	public void testRexCommandParse() {
		RexParser parser = new RexParser();
		Rex rex = (Rex) parser.parse(null, "rex field=_raw \"From: (?<from>.*) To: (?<to>.*)\"");

		assertEquals("_raw", rex.getInputField());
		assertEquals("from", rex.getOutputNames()[0]);
		assertEquals("to", rex.getOutputNames()[1]);
		assertEquals("From: (.*) To: (.*)", rex.getPattern().toString());
	}

	@Test
	public void testRexCommandParse2() {
		String s = "Close[00:00:20. SF. FIN] NAT[313]  R[16]";
		LogMap map = new LogMap();
		map.put("note", s);

		RexParser parser = new RexParser();
		Rex rex = (Rex) parser.parse(null, "rex field=note \"N(A|B)T\\[(?<nat>.*?)\\]  R\\[(?<r>.*?)\\]\"");
		DummyOutput out = new DummyOutput();
		rex.setNextCommand(out);

		rex.push(map);
		LogMap map2 = out.list.get(0);

		assertEquals("313", map2.get("nat"));
		assertEquals("16", map2.get("r"));
	}

	@Test
	public void testEscape() {
		String s = "rex field=line \"(?<d>\\d+-\\d+-\\d+)\"";
		RexParser parser = new RexParser();
		Rex rex = (Rex) parser.parse(null, s);
		assertEquals("(\\d+-\\d+-\\d+)", rex.getPattern().toString());
	}
}
