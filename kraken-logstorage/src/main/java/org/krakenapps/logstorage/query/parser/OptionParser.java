package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.StringPlaceholder;

public class OptionParser implements QueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("option", this, option(new StringPlaceholder(':'), new StringPlaceholder(), option(ref("option"))));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Binding b) {
		if (b == null)
			return new HashMap<Object, Object>();

		Map<Object, Object> option = null;
		if (b.getChildren().length < 3)
			option = new HashMap<Object, Object>();
		else
			option = (Map<Object, Object>) b.getChildren()[2].getValue();
		option.put(b.getChildren()[0].getValue(), b.getChildren()[1].getValue());

		return option;
	}
}
