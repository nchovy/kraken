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
package org.krakenapps.rule.http;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableRegexRule extends HttpRequestRule {
	private Map<String, ParameterValue> params;

	public VariableRegexRule(String id, String msg, String path, Map<String, ParameterValue> params) {
		super("regex", id, msg, path);
		this.params = params;
	}

	@Override
	public boolean match(HttpRequestContext c) {
		for (String key : params.keySet()) {
			String value = c.getParameters().get(key);
			if (value == null)
				return false;

			ParameterValue val = params.get(key);
			if (val.isRegex()) {
				Matcher matcher = val.getPattern().matcher(value);
				if (!matcher.find())
					return false;
			} else {
				if (!value.equals(val.getValue()))
					return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return getId() + " " + getMessage();
	}

	public static class ParameterValue {
		private String value;
		private boolean regex;
		private Pattern pattern;

		public ParameterValue(String value) {
			this(value, false);
		}

		public ParameterValue(String value, boolean isRegex) {
			this.value = value;
			this.regex = isRegex;
			if (isRegex)
				pattern = Pattern.compile(value);
		}

		public String getValue() {
			return value;
		}

		public boolean isRegex() {
			return regex;
		}

		public Pattern getPattern() {
			return pattern;
		}
	}

}
