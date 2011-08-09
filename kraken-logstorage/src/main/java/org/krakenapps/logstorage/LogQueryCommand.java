package org.krakenapps.logstorage;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.krakenapps.bnf.Syntax;

import org.krakenapps.logstorage.query.FileBufferList;
import org.krakenapps.logstorage.query.command.Lookup;
import org.krakenapps.logstorage.query.command.Table;
import org.krakenapps.logstorage.query.parser.EvalParser;
import org.krakenapps.logstorage.query.parser.LookupParser;
import org.krakenapps.logstorage.query.parser.OptionParser;
import org.krakenapps.logstorage.query.parser.QueryParser;
import org.krakenapps.logstorage.query.parser.RenameParser;
import org.krakenapps.logstorage.query.parser.SortParser;
import org.krakenapps.logstorage.query.parser.FunctionParser;
import org.krakenapps.logstorage.query.parser.StatsParser;
import org.krakenapps.logstorage.query.parser.TableParser;
import org.krakenapps.logstorage.query.parser.TimechartParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public abstract class LogQueryCommand {
	public static enum Status {
		Waiting, Running, End
	}

	private static Logger logger = LoggerFactory.getLogger(LogQueryCommand.class);
	private static Syntax syntax = new Syntax();
	private String query;
	protected String[] header;
	protected LogQueryCommand next;
	protected volatile Status status = Status.Waiting;

	static {
		List<Class<? extends QueryParser>> parsers = Arrays.asList(EvalParser.class, FunctionParser.class,
				LookupParser.class, OptionParser.class, RenameParser.class, SortParser.class, StatsParser.class,
				TableParser.class, TimechartParser.class);

		for (Class<? extends QueryParser> parser : parsers) {
			try {
				parser.newInstance().addSyntax(syntax);
			} catch (Exception e) {
				logger.error("kraken logstorage: failed to add syntax: " + parser.getSimpleName(), e);
			}
		}
	}

	public static LogQueryCommand createCommand(LogQueryService service, LogStorage logStorage,
			LogTableRegistry tableRegistry, String query) throws ParseException {
		LogQueryCommand token = (LogQueryCommand) syntax.eval(query);
		token.query = query;
		if (token instanceof Table) {
			((Table) token).setStorage(logStorage);
			TableMetadata tm = tableRegistry.getTableMetadata(tableRegistry.getTableId(((Table) token).getTableName()));
			if (tm.get("logformat") != null)
				((Table) token).setDataHeader(tm.get("logformat").split(" "));
		} else if (token instanceof Lookup) {
			((Lookup) token).setLogQueryService(service);
		}

		return token;
	}

	public String getQueryString() {
		return query;
	}

	public String[] getDataHeader() {
		return header;
	}

	public void setDataHeader(String[] header) {
		this.header = header;
	}

	protected Object getData(String key, Map<String, Object> m) {
		if (m.containsKey(key))
			return m.get(key);

		if (header != null) {
			for (int i = 0; i < header.length; i++) {
				if (header[i].equals(key)) {
					String data = (String) m.get("_data");
					int l = 0;
					for (int j = 0; j < i; j++) {
						l = data.indexOf(' ', l) + 1;
						if (l == 0)
							return null;
					}
					int r = data.indexOf(' ', l);

					String value = null;
					if (r == -1)
						value = data.substring(l);
					else
						value = data.substring(l, r);

					m.put(key, value);
					return value;
				}
			}
		}

		return null;
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

	public abstract void push(Map<String, Object> m);

	public void push(FileBufferList<Map<String, Object>> buf) {
		for (Map<String, Object> m : buf)
			push(m);
		buf.close();
	}

	protected void write(Map<String, Object> m) {
		if (next != null && next.status != Status.End) {
			next.status = Status.Running;
			next.push(m);
		}
	}

	protected void write(FileBufferList<Map<String, Object>> buf) {
		if (next != null && next.status != Status.End) {
			next.status = Status.Running;
			next.push(buf);
		} else {
			buf.close();
		}
	}

	public void eof() {
		status = Status.End;
		if (next != null)
			next.eof();
	}
}
