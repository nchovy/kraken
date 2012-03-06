package org.krakenapps.logdb.query.command;

import org.krakenapps.logdb.LogQueryCommand;

public class Drop extends LogQueryCommand {
	@Override
	public void push(LogMap m) {
	}

	@Override
	public boolean isReducer() {
		return true;
	}
}
