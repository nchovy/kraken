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
 package org.krakenapps.sonar.passive.ids.rule;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;

public class RuleBodyParser implements Parser {

	@Override
	public Object parse(Binding b) {
		List<RuleOption> options = new ArrayList<RuleOption>();
		parseOption(b, options);
		return options;
	}

	private void parseOption(Binding b, List<RuleOption> options) {
		if (b.getValue() != null && b.getValue() instanceof RuleOption){
			RuleOption option = (RuleOption) b.getValue();
			options.add(option);
		}
		
		if (b.getChildren() == null)
			return;

		for (Binding child : b.getChildren()) {
			parseOption(child, options);
		}
	}
}
