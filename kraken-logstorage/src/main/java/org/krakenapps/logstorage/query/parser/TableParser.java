package org.krakenapps.logstorage.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import java.util.Calendar;
import java.util.Date;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.logstorage.query.StringPlaceholder;
import org.krakenapps.logstorage.query.command.Table;

public class TableParser implements QueryParser {
	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("table", new TableParser(), k("table"), new StringPlaceholder(), option(uint()),
				option(k("duration"), k("="), uint(), choice(k("s"), k("m"), k("h"), k("d"), k("w"), k("mon"))));
		syntax.addRoot("table");
	}

	@Override
	public Object parse(Binding b) {
		String tableName = (String) b.getChildren()[1].getValue();

		if (b.getChildren().length == 3) {
			if (b.getChildren()[2].getValue() != null)
				return new Table(tableName, (Integer) b.getChildren()[2].getValue());
			else {
				int value = (Integer) b.getChildren()[2].getChildren()[2].getValue();
				String field = (String) b.getChildren()[2].getChildren()[3].getValue();
				return new Table(tableName, getDuration(value, field), null);
			}
		} else if (b.getChildren().length == 4) {
			int value = (Integer) b.getChildren()[3].getChildren()[2].getValue();
			String field = (String) b.getChildren()[3].getChildren()[3].getValue();
			return new Table(tableName, (Integer) b.getChildren()[2].getValue(), getDuration(value, field), null);
		}

		return new Table(tableName);
	}

	private Date getDuration(int value, String field) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		if (field.equals("s"))
			c.add(-value, Calendar.SECOND);
		else if (field.equals("m"))
			c.add(-value, Calendar.MINUTE);
		else if (field.equals("h"))
			c.add(-value, Calendar.HOUR_OF_DAY);
		else if (field.equals("d"))
			c.add(-value, Calendar.DAY_OF_MONTH);
		else if (field.equals("w"))
			c.add(-value, Calendar.WEEK_OF_YEAR);
		else if (field.equals("mon"))
			c.add(-value, Calendar.MONTH);
		return c.getTime();
	}
}
