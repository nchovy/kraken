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

import java.util.List;

public class Rule {
	private String id;
	private String name;
	private List<RuleOption> optionList;

	public Rule(List<RuleOption> options) {
		optionList = options;
		id = find(optionList, "id");
		name = find(optionList, "name");
	}

	public String find(String name) {
		for (RuleOption o : this.optionList)
			if (o.getName().equals(name))
				return o.getValue();
		return null;
	}
	private String find(List<RuleOption> options, String name) {
		for (RuleOption o : options)
			if (o.getName().equals(name))
				return o.getValue();

		return null;
	}

	@Override
	public String toString() {
		return String.format("SONAR-RULE/%s (%s)", id, name);
	}

}
