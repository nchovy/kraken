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
package org.krakenapps.log.api;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class DelimiterParser implements LogParser {
	private final String delimiter;
	private final String[] columnHeaders;
	private final String targetField;

	public DelimiterParser(String delimiter, String[] columnHeaders) {
		this(delimiter, columnHeaders, "line");
	}

	public DelimiterParser(String delimiter, String[] columnHeaders, String targetField) {
		this.delimiter = delimiter;
		this.columnHeaders = columnHeaders;
		this.targetField = targetField;
	}

	@Override
	public Map<String, Object> parse(Map<String, Object> params) {
		String line = (String) params.get(targetField);
		if (line == null)
			return params;

		HashMap<String, Object> m = new HashMap<String, Object>(40);

		StringTokenizer tok = new StringTokenizer(line, delimiter);
		int i = 0;
		while (tok.hasMoreTokens()) {
			String token = tok.nextToken();
			if (token != null)
				token = token.trim();

			if (columnHeaders != null && i < columnHeaders.length)
				m.put(columnHeaders[i], token);
			else
				m.put("column" + Integer.toString(i), token);
			i++;
		}

		return m;
	}

}
