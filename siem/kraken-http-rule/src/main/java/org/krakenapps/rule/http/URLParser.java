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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class URLParser {
	private URLParser() {
	}

	/**
	 * Parse url string and create request context
	 * 
	 * @param method
	 *            OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, or CONNECT
	 * @param url
	 *            path with querystring excluding domain
	 * @return
	 */
	public static HttpRequestContext parse(String method, String url) {
		String path = null;
		String queryString = null;

		int qpos = url.indexOf('?');
		if (qpos >= 0) {
			path = url.substring(0, qpos);
			queryString = url.substring(qpos + 1);
		} else {
			path = url;
		}

		return parse(method, path, queryString);
	}

	public static HttpRequestContext parse(String method, String path, String queryString) {
		Map<String, String> params = parseQueryString(queryString);
		return new HttpRequestContext(method, path, params);
	}

	private static Map<String, String> parseQueryString(String queryString) {
		Map<String, String> params = new HashMap<String, String>();
		if (queryString == null)
			return params;

		StringTokenizer tokenizer = new StringTokenizer(queryString, "&");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			int pos = token.indexOf('=');
			if (pos >= 0) {
				String key = token.substring(0, pos);
				String value = token.substring(pos + 1);
				if (key != null)
					params.put(key, value);
			}
		}

		return params;
	}
}
