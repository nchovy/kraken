package org.krakenapps.logstorage.query.command;

import java.util.Map;

import org.krakenapps.logstorage.LogQueryCommand;

public class Rename extends LogQueryCommand {
	private String from;
	private String to;

	public Rename(String from, String to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public void setDataHeader(String[] header) {
		for (int i = 0; i < header.length; i++) {
			if (header[i].equals(from))
				header[i] = to;
		}

		super.setDataHeader(header);
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
