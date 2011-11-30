package org.krakenapps.logdb.query.command;

import java.util.Map;

import org.krakenapps.logdb.LogQueryCommand;

public class Eval extends LogQueryCommand {
	private Term term;
	private String column;

	public Eval(Term term) {
		this(term, term.toString());
	}

	public Eval(Term term, String column) {
		this.term = term;
		this.column = column;
	}

	@Override
	public void push(Map<String, Object> m) {
		m.put(column, term.eval(m));
		write(m);
	}

	@Override
	public boolean isReducer() {
		return false;
	}
}
