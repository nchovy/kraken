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

import static org.mockito.Mockito.mock;

import org.junit.Ignore;
import org.junit.Test;
import org.krakenapps.log.api.LogParserFactoryRegistry;

public class ZipFileParserTest {
	@Ignore
	@Test
	public void testSimpleCase() {
		// TODO: add test zip resource file
		LogParserFactoryRegistry registry = mock(LogParserFactoryRegistry.class);
		ZipFileParser p = new ZipFileParser(registry);

		p.parse(null, "zipfile log.zip iis.log");
	}
}
