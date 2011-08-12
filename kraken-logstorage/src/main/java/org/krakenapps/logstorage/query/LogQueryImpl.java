package org.krakenapps.logstorage.query;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.logstorage.LogQuery;
import org.krakenapps.logstorage.LogQueryCallback;
import org.krakenapps.logstorage.LogQueryCommand;
import org.krakenapps.logstorage.LogQueryService;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.logstorage.LogQueryCommand.Status;
import org.krakenapps.logstorage.LogTimelineCallback;
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
	private Set<LogTimelineCallback> timelineCallbacks = new HashSet<LogTimelineCallback>();

	public LogQueryImpl(LogQueryService service, LogStorage logStorage, LogTableRegistry tableRegistry, String query) {
		this.queryString = query;

		for (String q : queryString.split("\\|")) {
			q = q.trim();
			try {
				commands.add(LogQueryCommand.createCommand(service, this, logStorage, tableRegistry, q));
			} catch (ParseException e) {
				throw new IllegalArgumentException("invalid query command: " + q);
			}
		}

		for (int i = 0; i < commands.size() - 1; i++)
			commands.get(i).setNextCommand(commands.get(i + 1));
		for (int i = 1; i < commands.size(); i++)
			commands.get(i).setDataHeader(commands.get(i - 1).getDataHeader());

		try {
			result = new Result();
		} catch (IOException e) {
			logger.error("kraken logstorage: cannot create result command", e);
		}
		commands.get(commands.size() - 1).setNextCommand(result);
		result.setDataHeader(commands.get(commands.size() - 1).getDataHeader());
	}

	@Override
	public void run() {
		logger.trace("kraken logstorage: run query => {}", queryString);
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
	public FileBufferList<Map<String, Object>> getResult() {
		if (result != null)
			return result.getResult();
		return null;
	}

	@Override
	public List<Map<String, Object>> getResult(int offset, int limit) {
		if (result != null)
			return result.getResult(offset, limit);
		return null;
	}

	@Override
	public List<LogQueryCommand> getCommands() {
		return commands;
	}

	@Override
	public void registerQueryCallback(LogQueryCallback callback) {
		result.registerCallback(callback);
	}

	@Override
	public void unregisterQueryCallback(LogQueryCallback callback) {
		result.unregisterCallback(callback);
	}

	@Override
	public Set<LogTimelineCallback> getTimelineCallbacks() {
		return timelineCallbacks;
	}

	@Override
	public void registerTimelineCallback(LogTimelineCallback callback) {
		timelineCallbacks.add(callback);
	}

	@Override
	public void unregisterTimelineCallback(LogTimelineCallback callback) {
		timelineCallbacks.remove(callback);
	}
}
