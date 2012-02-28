package org.krakenapps.logdb.query.parser;

import static org.krakenapps.bnf.Syntax.k;
import static org.krakenapps.bnf.Syntax.repeat;
import static org.krakenapps.bnf.Syntax.rule;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.LogScript;
import org.krakenapps.logdb.LogScriptRegistry;
import org.krakenapps.logdb.impl.ResourceManager;
import org.krakenapps.logdb.query.StringPlaceholder;
import org.krakenapps.logdb.query.command.Script;
import org.osgi.framework.BundleContext;

public class ScriptParser implements LogQueryParser {
	private BundleContext bc;
	private LogScriptRegistry scriptRegistry;

	public ScriptParser(ResourceManager resman) {
		this.bc = resman.get(BundleContext.class);
		this.scriptRegistry = resman.get(LogScriptRegistry.class);
	}

	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("script", this, k("script "), new StringPlaceholder(), repeat(rule(new StringPlaceholder())));
		syntax.addRoot("script");
	}

	@Override
	public Object parse(Binding b) {
		String name = (String) b.getChildren()[1].getValue();
		LogScript script = scriptRegistry.getScript(name);
		if (script == null)
			throw new IllegalArgumentException("log script not found: " + name);

		List<String> args = new ArrayList<String>();
		if (b.getChildren().length >= 3)
			parseArgs(args, b.getChildren()[2]);

		return new Script(bc, script, args.toArray(new String[0]));
	}

	private void parseArgs(List<String> args, Binding b) {
		if (b.getValue() != null)
			args.add((String) b.getValue());
		else {
			if (b.getChildren() != null) {
				for (Binding c : b.getChildren())
					parseArgs(args, c);
			}
		}
	}
}