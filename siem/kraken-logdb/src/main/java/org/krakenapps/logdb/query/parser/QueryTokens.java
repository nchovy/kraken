/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logdb.query.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryTokens {
	private String queryString;
	private List<QueryToken> tokens;

	public QueryTokens(String queryString, List<QueryToken> tokens) {
		this.queryString = queryString;
		this.tokens = tokens;
	}

	public String query() {
		return queryString;
	}

	public int size() {
		return tokens.size();
	}

	public QueryToken token(int offset) {
		return tokens.get(offset);
	}

	public String string(int offset) {
		return tokens.get(offset).token;
	}

	public List<QueryToken> subtokens(int begin, int end) {
		return tokens.subList(begin, end);
	}

	public List<String> substrings(int begin) {
		return substrings(begin, tokens.size());
	}

	public List<String> substrings(int begin, int end) {
		List<QueryToken> l = tokens.subList(begin, end);
		List<String> s = new ArrayList<String>();
		for (QueryToken t : l)
			s.add(t.token);
		return s;
	}

	public Map<String, String> options() {
		Map<String, String> options = new HashMap<String, String>();

		// TODO: consider quote-string and backslash escape
		for (QueryToken t : tokens) {
			String s = t.token;
			int p = s.indexOf('=');
			if (p < 0)
				continue;

			String key = s.substring(0, p);
			String value = s.substring(p + 1);
			options.put(key, value);
		}

		return options;
	}

	public String reverseArg(int offset) {
		return tokens.get(tokens.size() - offset - 1).token;
	}

	public String firstArg() {
		return tokens.get(1).token;
	}

	public String lastArg() {
		return tokens.get(tokens.size() - 1).token;
	}
}
