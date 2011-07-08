package org.krakenapps.logstorage.query.command;

import java.util.Map;

import org.krakenapps.logstorage.query.LogQueryCommand;

public class Rename extends LogQueryCommand {
	private String from;
	private String to;

	public Rename(String from, String to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public void push(Map<String, Object> m) {
		if (m.containsKey(from)) {
			m.put(to, m.get(from));
			m.remove(from);
		}
		write(m);
	}
}
