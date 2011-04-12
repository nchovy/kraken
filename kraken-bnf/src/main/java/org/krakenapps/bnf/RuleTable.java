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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleTable {
	private List<String> roots;
	private Map<String, Rule> ruleMap;
	private Map<String, Parser> parserMap;

	public RuleTable() {
		roots = new ArrayList<String>();
		ruleMap = new HashMap<String, Rule>();
		parserMap = new HashMap<String, Parser>();
	}

	public Rule getRule(String symbol) {
		return ruleMap.get(symbol);
	}

	public Parser getParser(String target) {
		return parserMap.get(target);
	}

	public void add(String symbol, Rule rule, Parser parser) {
		ruleMap.put(symbol, rule);
		parserMap.put(symbol, parser);
	}

	public void addRoot(String symbol) {
		if (roots.contains(symbol))
			return;

		roots.add(symbol);
	}

	public List<Rule> getRoots() {
		List<Rule> rules = new ArrayList<Rule>();
		for (String symbol : roots) {
			rules.add(new Reference(symbol));
		}
		return rules;
	}
}
