package org.krakenapps.logstorage.query.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.krakenapps.logstorage.query.LogQueryCommand;

public class Result extends LogQueryCommand {
	private List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

	@Override
	public void push(Map<String, Object> m) {
		result.add(m);
	}

	public List<Map<String, Object>> getResult() {
		return result;
	}
}
