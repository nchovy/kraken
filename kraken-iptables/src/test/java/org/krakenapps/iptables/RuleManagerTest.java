/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.iptables;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.krakenapps.iptables.impl.CommandRunner;
import org.krakenapps.iptables.impl.IptablesService;

import static org.junit.Assert.*;

public class RuleManagerTest {
	@Ignore
	@Test
	public void testList() throws IOException {
		IptablesService rm = new IptablesService();
		CommandRunner runner = new DummyCommandRunner(new File("src/test/resources/1.output"));
		rm.setCommandRunner(runner);

		List<Rule> rules = rm.getRules(Chain.INPUT.name());
		assertEquals(14, rules.size());

		for (int i = 0; i < 13; i++)
			assertEquals("ACCEPT", rules.get(i).getTarget());

		assertEquals("REJECT", rules.get(13).getTarget());
	}

	@Ignore
	@Test
	public void test2() throws IOException {
		IptablesService rm = new IptablesService();
		CommandRunner runner = new DummyCommandRunner(new File("src/test/resources/2.output"));
		rm.setCommandRunner(runner);

		List<Rule> rules = rm.getRules(Chain.OUTPUT.name());
		assertNotNull(rules);
	}
	
	@Test
	public void test3() throws IOException {
		IptablesService iptables = new IptablesService();
		CommandRunner runner = new DummyCommandRunner(new File("src/test/resources/3.output"));
		iptables.setCommandRunner(runner);
		
		List<String> chains = iptables.getChainNames();
		assertNotNull(chains);
		
		assertEquals(4, chains.size());
		assertEquals("INPUT", chains.get(0));
		assertEquals("FORWARD", chains.get(1));
		assertEquals("OUTPUT", chains.get(2));
		assertEquals("RH-Firewall-1-INPUT", chains.get(3));
		
		List<Rule> inputRules = iptables.getRules("INPUT");
		assertEquals(1, inputRules.size());
		
		List<Rule> outputRules = iptables.getRules("OUTPUT");
		assertEquals(3, outputRules.size());
		
		List<Rule> rhRules = iptables.getRules("RH-Firewall-1-INPUT");
		assertEquals(21, rhRules.size());
		
		for (Rule r : outputRules)
			System.out.println(r);
	}
}
