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
package org.krakenapps.logparser.syslog.juniper.attack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JuniperAttackLogPattern {
	public static final String SEVERITY_KEY = "severity";
	public static final String ID_KEY = "id";
	public static final String RULE_KEY = "rule";
	public static final String CATEGORY_KEY = "category";
	public static final String CATEGORY_VALUE = "intrusion";

	private String severity;
	private String id;
	private String rule;
	private List<String> constElements;
	private List<LogVariableType> variables;

	private JuniperAttackLogPattern(String severity, String id, String rule, List<String> constElements, List<LogVariableType> variables) {
		this.severity = severity;
		this.id = id;
		this.rule = rule;
		this.constElements = constElements;
		this.variables = variables;
	}

	public static JuniperAttackLogPattern from(String category, String patternString) {
		int categorySeperaterPosition = category.indexOf(' ');
		if (categorySeperaterPosition == -1)
			return null;
		String severity = category.substring(0, categorySeperaterPosition);
		String id = category.substring(categorySeperaterPosition + 2, category.length() - 1);

		String rule = patternString.substring(0, patternString.indexOf('!'));

		List<String> constElements = new ArrayList<String>();
		List<LogVariableType> variables = new ArrayList<LogVariableType>();
		int offset = 0;
		do {
			int[] pos = findBraket(patternString, offset);
			if (pos[1] == -1) {
				constElements.add(patternString.substring(offset, patternString.length()));
				break;
			}
			constElements.add(patternString.substring(offset, pos[0]));
			variables.add(LogVariableType.from(patternString.substring(pos[0], pos[1])));
			offset = pos[1];
		} while (offset > -1 && offset < patternString.length());

		return new JuniperAttackLogPattern(severity, id, rule, Collections.unmodifiableList(constElements),
				Collections.unmodifiableList(variables));
	}

	public Map<String, Object> parse(String line) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(JuniperAttackLogPattern.SEVERITY_KEY, severity);
		map.put(JuniperAttackLogPattern.ID_KEY, id);
		map.put(JuniperAttackLogPattern.RULE_KEY, rule);
		map.put(CATEGORY_KEY, CATEGORY_VALUE);

		int beginIndex = 0;
		int endIndex = 0;
		int variableIndex = 0;
		int variableIndexIncre = 1;
		String current = constElements.get(0);
		for (int i = 1; i < constElements.size(); i++) {
			String next = constElements.get(i);
			while (!line.regionMatches(endIndex, next, 0, next.length()) && endIndex < line.length())
				endIndex++;

			if (endIndex >= line.length()) {
				if (":".equals(next)) {
					endIndex = beginIndex;
					variableIndexIncre = 2;
					continue;
				}
				return null;
			}

			LogVariableType variable = variables.get(variableIndex);
			Object value = variable.parse(line.substring(beginIndex + current.length(), endIndex));

			map.put(variable.toString(), value);
			beginIndex = endIndex;
			current = next;
			variableIndex += variableIndexIncre;
			variableIndexIncre = 1;
		}

		return map;
	}

	public List<String> getConstElements() {
		return constElements;
	}

	public List<LogVariableType> getVariables() {
		return variables;
	}

	static int[] findBraket(String patternString, int offset) {
		int start = -1, end = -1;

		int braket = -1;

		for (int i = offset; i < patternString.length() && end == -1; i++) {
			int ch = patternString.charAt(i);

			if (braket == -1) {
				switch (ch) {
				case '{':
				case '<':
					start = i;
					braket = ch;
				}
			}

			else if (ch == braket) {
				start = i;
			}

			// '<'+2 = '>', '{'+2 = '}'
			else if (ch == (braket + 2)) {
				end = i + 1;
			}

		}

		int[] result = { start, end };
		return result;
	}
}
