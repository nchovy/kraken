package org.krakenapps.logdb.query.command;

import java.util.Map;

import org.krakenapps.logdb.LogQueryCommand;

public class Drop extends LogQueryCommand {
	@Override
	public void push(Map<String, Object> m) {
	}

	@Override
	public boolean isReducer() {
		return true;
	}
}
