package org.krakenapps.logdb.arbiter.impl;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.logdb.arbiter.ArbiterQueryStatus;
import org.krakenapps.logdb.arbiter.ArbiterService;

public class ArbiterScript implements Script {
	private ArbiterService arbiter;
	private ScriptContext context;

	public ArbiterScript(ArbiterService arbiter) {
		this.arbiter = arbiter;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	/**
	 * print all connected nodes
	 */
	public void nodes(String[] args) {

	}

	/**
	 * print all distributed queries
	 */
	public void queries(String[] args) {
		context.println("Arbiter Queries");
		context.println("-----------------");

		for (ArbiterQueryStatus q : arbiter.getQueries())
			context.println(q);
	}

	public void createQuery(String[] args) {
		ArbiterQueryStatus q = arbiter.createQuery(args[0]);
		context.println(q);
	}

	public void startQuery(String[] args) {
		arbiter.startQuery(args[0]);
	}

	public void removeQuery(String[] args) {
		arbiter.removeQuery(args[0]);
	}
}
