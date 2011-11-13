package org.krakenapps.logdb.query.command;

import java.util.Map;

import org.krakenapps.logdb.LogQueryCommand;

public class Rpc extends LogQueryCommand {
	/**
	 * dist query guid
	 */
	private String guid;

	private boolean sender;

	public Rpc(String guid, boolean sender) {
		this.guid = guid;
		this.sender = sender;
	}

	@Override
	public void push(Map<String, Object> m) {
		write(m);
	}

	@Override
	public boolean isReducer() {
		return false;
	}

	@Override
	public void start() {
		status = Status.Running;
	}

	@Override
	public String toString() {
		return "RPC " + (sender ? "Output" : "Input") + guid;
	}

}
