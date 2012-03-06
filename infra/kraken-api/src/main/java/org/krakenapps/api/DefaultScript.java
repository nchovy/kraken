package org.krakenapps.api;

public abstract class DefaultScript implements Script {
	protected ScriptContext context;

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}
}
