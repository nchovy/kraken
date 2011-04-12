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
package org.krakenapps.filter.impl;

import org.junit.*;
import org.krakenapps.filter.ComponentDescription;
import org.krakenapps.filter.ComponentDescriptionParser;

import static org.junit.Assert.*;

public class ComponentDescriptionParserTest {
	private final String sampleDescription1 = "factory name=\"rssReader\" " + "state=\"valid\" bundle=\"6\" "
			+ "implementation-class=\"org.krakenapps.rss.impl.RssReaderImpl\"\n"
			+ "\tprovides specification=\"org.krakenapps.rss.RssReader\"\n"
			+ "\tmissinghandlers list=\"[]\"\n"
			+ "\trequiredhandlers list=\"[org.apache.felix.ipojo:provides, "
			+ "org.apache.felix.ipojo:architecture]\"\n";

	@Test
	public void testParse() {
		ComponentDescription description = ComponentDescriptionParser.parse("sampleReader", sampleDescription1);
		assertEquals("rssReader", description.getFactoryName());
		assertEquals("valid", description.getState());
		assertEquals(6, description.getBundleId());
		assertEquals("org.krakenapps.rss.impl.RssReaderImpl", description.getImplementationClass());
		assertEquals("org.krakenapps.rss.RssReader", description.getSpecifications().get(0));
		assertEquals(0, description.getMissingHandlers().size());
		assertEquals("org.apache.felix.ipojo:provides", description.getRequiredHanlders().get(0));
		assertEquals("org.apache.felix.ipojo:architecture", description.getRequiredHanlders().get(1));
	}

	private final String sampleDescription2 = "factory name="
			+ "\"org.krakenapps.filter.examples.RandomLogGenerator\" state=\"valid\" bundle=\"8\" "
			+ "implementation-class=\"org.krakenapps.filter.examples.RandomLogGenerator\"\n"
			+ "\tprovides specification=\"java.lang.Runnable\"\n"
			+ "\tprovides specification=\"org.krakenapps.filter.Filter\"\n"
			+ "\tmissinghandlers list=\"[]\"\n"
			+ "\trequiredhandlers list=\"[org.apache.felix.ipojo:callback, "
			+ "org.apache.felix.ipojo:provides, org.apache.felix.ipojo:architecture]\"\n";

	@Test
	public void testParse2() {
		ComponentDescription description = ComponentDescriptionParser.parse("sampleReader", sampleDescription2);
		assertEquals("org.krakenapps.filter.examples.RandomLogGenerator", description.getFactoryName());
		assertEquals("valid", description.getState());
		assertEquals(8, description.getBundleId());
		assertEquals("org.krakenapps.filter.examples.RandomLogGenerator", description
				.getImplementationClass());
		assertEquals("java.lang.Runnable", description.getSpecifications().get(0));
		assertEquals("org.krakenapps.filter.Filter", description.getSpecifications().get(1));
		assertEquals(0, description.getMissingHandlers().size());
		assertEquals("org.apache.felix.ipojo:callback", description.getRequiredHanlders().get(0));
		assertEquals("org.apache.felix.ipojo:provides", description.getRequiredHanlders().get(1));
		assertEquals("org.apache.felix.ipojo:architecture", description.getRequiredHanlders().get(2));
	}
}
