package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.StringPlaceholder;
import org.krakenapps.logstorage.query.command.Rename;

public class RenameParser implements QueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("rename", this, k("rename"), new StringPlaceholder(), k("as"), new StringPlaceholder());
		syntax.addRoot("rename");
	}

	@Override
	public Object parse(Binding b) {
		String from = (String) b.getChildren()[1].getValue();
		String to = (String) b.getChildren()[3].getValue();
		return new Rename(from, to);
	}
}
