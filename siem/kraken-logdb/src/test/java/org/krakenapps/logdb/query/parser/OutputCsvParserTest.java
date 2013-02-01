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

import java.io.File;

import org.junit.Test;
import static org.junit.Assert.*;

import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.query.command.OutputCsv;

public class OutputCsvParserTest {
	@Test
	public void testNormalCase() {
		new File("logexport.csv").delete();
		OutputCsv csv = null;
		try {
			OutputCsvParser p = new OutputCsvParser();
			csv = (OutputCsv) p.parse(null, "outputcsv logexport.csv sip, dip ");

			File f = csv.getCsvFile();
			assertEquals("logexport.csv", f.getName());
			assertEquals("sip", csv.getFields().get(0));
			assertEquals("dip", csv.getFields().get(1));

		} finally {
			// to close csv file handle
			if (csv != null)
				csv.eof();
			new File("logexport.csv").delete();
		}
	}

	@Test
	public void testMissingField1() {
		new File("logexport.csv").delete();
		try {
			OutputCsvParser p = new OutputCsvParser();
			p.parse(null, "outputcsv logexport.csv ");
			fail();
		} catch (LogQueryParseException e) {
			assertEquals("missing-field", e.getType());
			assertEquals(24, (int) e.getOffset());
		} finally {
			new File("logexport.csv").delete();
		}
	}

	@Test
	public void testMissingField2() {
		new File("logexport.csv").delete();
		try {
			OutputCsvParser p = new OutputCsvParser();
			p.parse(null, "outputcsv logexport.csv sip,");
			fail();
		} catch (LogQueryParseException e) {
			assertEquals("missing-field", e.getType());
			assertEquals(28, (int) e.getOffset());
		} finally {
			new File("logexport.csv").delete();
		}
	}
}
