package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Parser;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.StringPlaceholder;
import org.krakenapps.logstorage.query.command.Lookup;

public class LookupParser implements QueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("lookup", this, k("lookup"), ref("option"), new StringPlaceholder(), ref("lookup_field"),
				k("OUTPUT"), ref("lookup_field"));
		syntax.add("lookup_field", new LookupFieldParser(), new StringPlaceholder(),
				option(k("as"), new StringPlaceholder()));
		syntax.addRoot("lookup");
	}

	@Override
	public Object parse(Binding b) {
		String tableName = (String) b.getChildren()[2].getValue();
		LookupField src = (LookupField) b.getChildren()[3].getValue();
		LookupField dst = (LookupField) b.getChildren()[5].getValue();
		return new Lookup(tableName, src.table, src.local, dst.table, dst.local);
	}

	private class LookupField {
		private String local;
		private String table;
	}

	public class LookupFieldParser implements Parser {
		@Override
		public Object parse(Binding b) {
			LookupField field = new LookupField();
			if (b.getValue() != null) {
				field.local = (String) b.getValue();
				field.table = (String) b.getValue();
			} else {
				field.local = (String) b.getChildren()[0].getValue();
				field.table = (String) b.getChildren()[1].getChildren()[1].getValue();
			}
			return field;
		}
	}
}
