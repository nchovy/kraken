package org.krakenapps.log.api;

import java.util.HashMap;
import java.util.Map;

public class DelimiterParser implements LogParser {
	private String delimiter;
	private String[] columnHeaders;

	public DelimiterParser(String delimiter, String[] columnHeaders) {
		this.delimiter = delimiter;
		this.columnHeaders = columnHeaders;
	}

	@Override
	public String getName() {
		return "delimiter";
	}

	@Override
	public Map<String, Object> parse(Map<String, Object> params) {
		String line = (String) params.get("line");
		if (line == null)
			return params;

		String[] tokens = line.split(delimiter);

		Map<String, Object> m = new HashMap<String, Object>(tokens.length);
		for (int i = 0; i < tokens.length; i++) {
			if (i < columnHeaders.length)
				m.put(columnHeaders[i], tokens[i].trim());
			else
				m.put("column" + Integer.toString(i), tokens[i]);
		}

		return m;
	}

}
