package org.krakenapps.logdb.query.parser;

import static org.krakenapps.bnf.Syntax.k;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogScript;
import org.krakenapps.logdb.LogScriptRegistry;
import org.krakenapps.logdb.query.StringPlaceholder;
import org.krakenapps.logdb.query.command.Script;
import org.osgi.framework.BundleContext;

public class ScriptParser implements QueryParser {
	private BundleContext bc;
	private LogScriptRegistry scriptRegistry;

	public ScriptParser(BundleContext bc, LogScriptRegistry scriptRegistry) {
		this.bc = bc;
		this.scriptRegistry = scriptRegistry;
	}

	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("script", this, k("script"), new StringPlaceholder());
		syntax.addRoot("script");
	}

	@Override
	public Object parse(Binding b) {
		String name = (String) b.getChildren()[1].getValue();
		LogScript script = scriptRegistry.getScript(name);
		if (script == null)
			throw new IllegalArgumentException("log script not found: " + name);

		return new Script(bc, script);
	}
}