package org.krakenapps.logstorage.query;

import java.text.ParseException;
import java.util.Map;

import org.krakenapps.bnf.Syntax;

import static org.krakenapps.bnf.Syntax.*;

import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.logstorage.TableMetadata;
import org.krakenapps.logstorage.query.command.Table;
import org.krakenapps.logstorage.query.parser.RenameParser;
import org.krakenapps.logstorage.query.parser.SortParser;
import org.krakenapps.logstorage.query.parser.StatsParser;
import org.krakenapps.logstorage.query.parser.TableParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LogQueryCommand {
	public static enum Status {
		Waiting, Running, End
	}

	private Logger logger = LoggerFactory.getLogger(LogQueryCommand.class);
	private static long ts;
	private String query;
	protected LogQueryCommand next;
	protected volatile Status status = Status.Waiting;

	public static void setTs(long ts) {
		LogQueryCommand.ts = ts;
	}

	public static LogQueryCommand createCommand(LogStorage logStorage, LogTableRegistry tableRegistry, String query)
			throws ParseException {
		Syntax s = new Syntax();

		s.add("table", new TableParser(), k("table"), new StringPlaceholder(), option(uint()));
		s.addRoot("table");

		s.add("rename", new RenameParser(), k("rename"), new StringPlaceholder(), k("as"), new StringPlaceholder());
		s.addRoot("rename");

		s.add("stats", new StatsParser(), k("stats"), option(k("allnum"), t("="), choice(k("true"), k("false"))),
				option(k("delim"), t("="), choice(k("true"), k("false"))), ref("stats_function"),
				option(k("by"), ref("stats_field")));
		s.add("stats_function", new StatsParser.StatsFunctionParser(), new FunctionPlaceholder(),
				option(k("as"), new StringPlaceholder()), option(t(",")), option(ref("stats_function")));
		s.add("stats_field", new StatsParser.StatsFieldParser(), new StringPlaceholder(','), option(ref("stats_field")));
		s.addRoot("stats");

		s.add("sort", new SortParser(), k("sort"), option(uint()), ref("sort_field"));
		s.add("sort_field", new SortParser.SortFieldParser(),
				repeat(rule(option(choice(t("+"), t("-"))), new StringPlaceholder(','))));
		s.addRoot("sort");

		LogQueryCommand token = (LogQueryCommand) s.eval(query);
		token.query = query;
		if (token instanceof Table) {
			((Table) token).setStorage(logStorage);
			TableMetadata tm = tableRegistry.getTableMetadata(tableRegistry.getTableId(((Table) token).getTableName()));
			((Table) token).setLogFormat(tm.get("logformat"));
		}

		return token;
	}

	public String getQueryString() {
		return query;
	}

	public LogQueryCommand getNextCommand() {
		return next;
	}

	public void setNextCommand(LogQueryCommand next) {
		this.next = next;
	}

	public Status getStatus() {
		return status;
	}

	public void start() {
		throw new UnsupportedOperationException();
	}

	protected abstract void push(Map<String, Object> m);

	public void write(Map<String, Object> m) {
		if (next != null && next.status != Status.End) {
			next.status = Status.Running;
			next.push(m);
		}
	}

	public void eof() {
		status = Status.End;
		long now = System.currentTimeMillis();
		logger.trace(getClass().getSimpleName() + " eof : " + (now - ts) + " ms");
		ts = now;
		if (next != null)
			next.eof();
	}
}
