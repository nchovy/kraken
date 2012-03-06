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
package org.krakenapps.rule.impl;

import java.util.Collection;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.rule.Rule;
import org.krakenapps.rule.RuleEngine;
import org.krakenapps.rule.RuleEngineRegistry;

public class RuleScript implements Script {
	private RuleEngineRegistry registry;
	private ScriptContext context;

	public RuleScript(RuleEngineRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void engines(String[] args) {
		context.println("Engines");
		context.println("----------------");
		for (RuleEngine engine : registry.getEngines()) {
			context.println(engine.getName() + " (" + engine.getRules().size() + "): " + engine.getDescription());
		}
	}

	public void rules(String[] args) {
		context.println("Rules");
		context.println("-----------------");

		for (RuleEngine engine : registry.getEngines()) {
			Collection<Rule> rules = engine.getRules();
			context.println("engine [" + engine.getName() + "] (" + rules.size() + ")");

			for (Rule rule : rules) {
				context.println("\t" + rule.toString());
			}
		}
	}
}
