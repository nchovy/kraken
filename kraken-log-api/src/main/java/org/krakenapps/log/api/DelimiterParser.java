package org.krakenapps.log.api;

import java.util.HashMap;
import java.util.Map;

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
		String line = null;
		if (params.containsKey(targetField))
			line = (String) params.get(targetField);

		if (line == null)
			return params;

		String[] tokens = line.split(delimiter);

		Map<String, Object> m = new HashMap<String, Object>(tokens.length);
		m.putAll(params);
		m.remove("line");
		for (int i = 0; i < tokens.length; i++) {
			if (i < columnHeaders.length)
				m.put(columnHeaders[i], tokens[i].trim());
			else
				m.put("column" + Integer.toString(i), tokens[i]);
		}

		return m;
	}

}
