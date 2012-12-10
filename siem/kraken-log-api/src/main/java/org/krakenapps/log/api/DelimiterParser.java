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
