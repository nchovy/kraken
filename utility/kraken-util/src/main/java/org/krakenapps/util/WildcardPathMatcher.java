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
package org.krakenapps.util;

import java.util.regex.Pattern;

public class WildcardPathMatcher {
	private Pattern pattern;

	public WildcardPathMatcher(String token) {
		pattern = Pattern.compile(wildcardToRegex(token));
	}

	private String wildcardToRegex(String token) {
		StringBuffer buf = new StringBuffer(token.length());
		buf.append("^");
		for (int i = 0, is = token.length(); i < is; ++i) {
			char ch = token.charAt(i);
			switch (ch) {
			case '*':
				// buf.append(".*?");
				buf.append("[^\\/\\\\]*?");
				break;
			case '\\':
			case '.':
			case '[':
			case ']':
			case '(':
			case ')':
			case '$':
			case '^':
			case '{':
			case '}':
			case '|':
				buf.append("\\");
				buf.append(ch);
				break;
			default:
				buf.append(ch);
				break;
			}
		}
		buf.append("$");
		return buf.toString();
	}

	public boolean isMatch(String name) {
		return pattern.matcher(name).matches();
	}

}