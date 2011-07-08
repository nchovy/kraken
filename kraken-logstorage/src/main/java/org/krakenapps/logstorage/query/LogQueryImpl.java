package org.krakenapps.logstorage.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.logstorage.LogQuery;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.logstorage.query.LogQueryCommand.Status;
import org.krakenapps.logstorage.query.command.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogQueryImpl implements LogQuery {
	private Logger logger = LoggerFactory.getLogger(LogQueryImpl.class);
	private static AtomicInteger nextId = new AtomicInteger(1);
	private final int id = nextId.getAndIncrement();
	private String queryString;
	private List<LogQueryCommand> commands = new ArrayList<LogQueryCommand>();
	private Result result;

	public LogQueryImpl(LogStorage logStorage, LogTableRegistry tableRegistry, String query) {
		this.queryString = query;

		for (String q : queryString.split("\\|")) {
			q = q.trim();
			try {
				commands.add(LogQueryCommand.createCommand(logStorage, tableRegistry, q));
			} catch (ParseException e) {
				System.out.println("parse exception");
				throw new IllegalArgumentException("invalid query command: " + q);
			}
		}

		for (int i = 0; i < commands.size() - 1; i++)
			commands.get(i).setNextCommand(commands.get(i + 1));
		result = new Result();
		commands.get(commands.size() - 1).setNextCommand(result);

		logger.trace("===== new query =====");
		LogQueryCommand.setTs(System.currentTimeMillis());
	}

	@Override
	public void run() {
		if (commands.size() > 0)
			commands.get(0).start();
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public boolean isEnd() {
		if (commands.size() == 0)
			return true;
		return result.getStatus().equals(Status.End);
	}

	@Override
	public void cancel() {
		for (LogQueryCommand command : commands) {
			if (command.getStatus() != Status.End)
				command.eof();
		}
	}

	@Override
	public List<Map<String, Object>> getResult() {
		if (result != null)
			return result.getResult();
		return null;
	}

	public Collection<LogQueryCommand> getCommands() {
		return commands;
	}
}
