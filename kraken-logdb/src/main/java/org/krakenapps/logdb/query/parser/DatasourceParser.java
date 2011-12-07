package org.krakenapps.logdb.query.parser;

import static org.krakenapps.bnf.Syntax.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.krakenapps.bnf.Binding;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.log.api.LogParserFactoryRegistry;
import org.krakenapps.logdb.DataSource;
import org.krakenapps.logdb.DataSourceRegistry;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.query.command.Datasource;
import org.krakenapps.logdb.query.command.OptionChecker;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTableRegistry;

public class DatasourceParser implements LogQueryParser {
	private DataSourceRegistry dataSourceRegistry;
	private LogStorage logStorage;
	private LogTableRegistry tableRegistry;
	private LogParserFactoryRegistry parserFactoryRegistry;

	public DatasourceParser(DataSourceRegistry dataSourceRegistry, LogStorage logStorage, LogTableRegistry tableRegistry,
			LogParserFactoryRegistry parserFactoryRegistry) {
		this.dataSourceRegistry = dataSourceRegistry;
		this.logStorage = logStorage;
		this.tableRegistry = tableRegistry;
		this.parserFactoryRegistry = parserFactoryRegistry;
	}

	@Override
	public void addSyntax(Syntax syntax) {
		syntax.add("datasource", this, k("datasource "), ref("option"), ref("option_checker"));
		syntax.addRoot("datasource");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object parse(Binding b) {
		Map<String, String> options = (Map<String, String>) b.getChildren()[1].getValue();
		List<DataSource> sources = new ArrayList<DataSource>();
		OptionChecker checker = (OptionChecker) b.getChildren()[2].getValue();
		for (DataSource source : dataSourceRegistry.getAll()) {
			if (checker.eval(source.getMetadata()))
				sources.add(source);
		}
		Datasource datasource = null;
		if (options.containsKey("cache"))
			datasource = new Datasource(sources);
		else {
			try {
				datasource = new Datasource(sources, Integer.parseInt(options.get("cache")));
			} catch (NumberFormatException e) {
				datasource = new Datasource(sources);
			}
		}
		datasource.setLogStorage(logStorage);
		datasource.setTableRegistry(tableRegistry);
		datasource.setParserFactoryRegistry(parserFactoryRegistry);

		if (options.containsKey("duration")) {
			String duration = options.get("duration");
			int i;
			for (i = 0; i < duration.length(); i++) {
				char c = duration.charAt(i);
				if (!('0' <= c && c <= '9'))
					break;
			}
			int value = Integer.parseInt(duration.substring(0, i));
			datasource.setFrom(getDuration(value, duration.substring(i)));
		}
		if (options.containsKey("from"))
			datasource.setFrom(getDate(options.get("from")));
		if (options.containsKey("to"))
			datasource.setTo(getDate(options.get("to")));

		if (options.containsKey("offset"))
			datasource.setOffset(Integer.parseInt(options.get("offset")));
		if (options.containsKey("limit"))
			datasource.setLimit(Integer.parseInt(options.get("limit")));

		return datasource;
	}

	private Date getDuration(int value, String field) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		if (field.equalsIgnoreCase("s"))
			c.add(Calendar.SECOND, -value);
		else if (field.equalsIgnoreCase("m"))
			c.add(Calendar.MINUTE, -value);
		else if (field.equalsIgnoreCase("h"))
			c.add(Calendar.HOUR_OF_DAY, -value);
		else if (field.equalsIgnoreCase("d"))
			c.add(Calendar.DAY_OF_MONTH, -value);
		else if (field.equalsIgnoreCase("w"))
			c.add(Calendar.WEEK_OF_YEAR, -value);
		else if (field.equalsIgnoreCase("mon"))
			c.add(Calendar.MONTH, -value);
		return c.getTime();
	}

	private Date getDate(String value) {
		String type1 = "yyyy";
		String type2 = "yyyyMM";
		String type3 = "yyyyMMdd";
		String type4 = "yyyyMMddHH";
		String type5 = "yyyyMMddHHmm";
		String type6 = "yyyyMMddHHmmss";

		SimpleDateFormat sdf = null;
		if (value.length() == 4)
			sdf = new SimpleDateFormat(type1);
		else if (value.length() == 6)
			sdf = new SimpleDateFormat(type2);
		else if (value.length() == 8)
			sdf = new SimpleDateFormat(type3);
		else if (value.length() == 10)
			sdf = new SimpleDateFormat(type4);
		else if (value.length() == 12)
			sdf = new SimpleDateFormat(type5);
		else if (value.length() == 14)
			sdf = new SimpleDateFormat(type6);

		if (sdf == null)
			throw new IllegalArgumentException();

		try {
			return sdf.parse(value);
		} catch (ParseException e) {
			return null;
		}
	}
}
