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
package org.krakenapps.rule.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GenericRule {
	private String id;
	private String message;
	private List<GenericRuleOption> options;

	public GenericRule(List<GenericRuleOption> options) {
		this.options = options;
		id = find(options, "id");
		message = find(options, "msg");
	}

	public String getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public String get(String name) {
		for (GenericRuleOption o : this.options)
			if (o.getName().equals(name))
				return o.getValue();
		return null;
	}

	public Collection<String> getAll(String name) {
		List<String> matches = new ArrayList<String>();
		for (GenericRuleOption o : this.options)
			if (o.getName().equals(name))
				matches.add(o.getValue());

		return matches;
	}
	
	public Collection<GenericRuleOption> getOptions() {
		return options;
	}

	private String find(List<GenericRuleOption> options, String name) {
		for (GenericRuleOption o : options)
			if (o.getName().equals(name))
				return o.getValue();

		return null;
	}

	@Override
	public String toString() {
		return String.format("RULE/%s (%s)", id, message);
	}

}
