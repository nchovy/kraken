package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.StringPlaceholder;
import org.krakenapps.logstorage.query.command.Table;

public class TableParser implements QueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("table", new TableParser(), k("table"), ref("option"), new StringPlaceholder(), option(uint()));
		syntax.addRoot("table");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Binding b) {
		Map<String, String> options = (Map<String, String>) b.getChildren()[1].getValue();
		String tableName = (String) b.getChildren()[2].getValue();
		Date from = null;
		int limit = 0;

		if (options.containsKey("duration")) {
			String duration = options.get("duration");
			int value = Integer.parseInt(duration.substring(0, duration.length() - 1));
			from = getDuration(value, duration.substring(duration.length() - 1));
		}

		if (options.containsKey("limit"))
			limit = Integer.parseInt(options.get("limit"));

		return new Table(tableName, limit, from, null);
	}

	private Date getDuration(int value, String field) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		if (field.equals("s"))
			c.add(Calendar.SECOND, -value);
		else if (field.equals("m"))
			c.add(Calendar.MINUTE, -value);
		else if (field.equals("h"))
			c.add(Calendar.HOUR_OF_DAY, -value);
		else if (field.equals("d"))
			c.add(Calendar.DAY_OF_MONTH, -value);
		else if (field.equals("w"))
			c.add(Calendar.WEEK_OF_YEAR, -value);
		else if (field.equals("mon"))
			c.add(Calendar.MONTH, -value);
		return c.getTime();
	}
}
