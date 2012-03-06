package org.krakenapps.logdb.cmd;

import java.util.Arrays;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.logdb.SyntaxProvider;

@Component(name = "logdb-cmd")
public class CmdExtender {
	@Requires
	private SyntaxProvider syntaxProvider;

	private CmdParser parser;

	@Validate
	public void start() {
		parser = new CmdParser();
		syntaxProvider.addParsers(Arrays.asList(parser));
	}

	@Invalidate
	public void stop() {
		if (syntaxProvider != null)
			syntaxProvider.removeParsers(Arrays.asList(parser));
	}
}
