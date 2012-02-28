/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.logdb.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryCallback;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryCommand.Status;
import org.krakenapps.logdb.LogTimelineCallback;
import org.krakenapps.logdb.SyntaxProvider;
import org.krakenapps.logdb.impl.ResourceManager;
import org.krakenapps.logdb.query.ResourceManagerImpl.CommandThread;
import org.krakenapps.logdb.query.command.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogQueryImpl implements LogQuery {
	private Logger logger = LoggerFactory.getLogger(LogQueryImpl.class);
	private static AtomicInteger nextId = new AtomicInteger(1);

	private final int id = nextId.getAndIncrement();
	private ResourceManager resman;
	private String queryString;
	private LinkedList<LogQueryCommand> commands = new LinkedList<LogQueryCommand>();
	private Date lastStarted;
	private Result result;
	private Set<LogQueryCallback> logQueryCallbacks = new HashSet<LogQueryCallback>();
	private Set<LogTimelineCallback> timelineCallbacks = new HashSet<LogTimelineCallback>();

	public LogQueryImpl(ResourceManager resman, String queryString) {
		this.resman = resman;
		this.queryString = queryString;

		for (String q : split(queryString)) {
			q = q.trim();
			try {
				LogQueryCommand cmd = resman.get(SyntaxProvider.class).eval(this, q);
				if (!commands.isEmpty())
					commands.getLast().setNextCommand(cmd);
				commands.add(cmd);
			} catch (ParseException e) {
				throw new IllegalArgumentException("invalid query command: " + q);
			}
		}

		if (commands.isEmpty())
			throw new IllegalArgumentException("empty query");

		LogQueryCommand callback = commands.getLast();
		ListIterator<LogQueryCommand> cmdIter = commands.listIterator(commands.size());
		while (cmdIter.hasPrevious()) {
			LogQueryCommand prev = cmdIter.previous();
			if (prev.isReducer() && cmdIter.hasPrevious()) {
				callback = cmdIter.previous();
				cmdIter.next();
			}
		}
		callback.setCallbackTimeline(true);
	}

	private static List<String> split(String query) {
		List<String> l = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		char before = 0;
		boolean b = false;

		for (char c : query.toCharArray()) {
			if (c == '"' && before != '\\') {
				b = !b;
				sb.append(c);
			} else {
				if (c == '|' && !b) {
					l.add(sb.toString());
					sb = new StringBuilder();
				} else
					sb.append(c);
			}
			before = c;
		}

		if (sb.length() > 0)
			l.add(sb.toString());

		return l;
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
		if (result == null)
			return true;
		if (commands.getFirst().getStatus() == Status.Waiting)
			return true;
		return result.getStatus().equals(Status.End);
	}

	@Override
	public void start() {
		if (!isEnd())
			throw new IllegalStateException("already running");

		lastStarted = new Date();
		try {
			if (result != null)
				result.closeResult();

			result = new Result();
			result.setLogQuery(this);
			result.setExecutorService(resman.getExecutorService());
			for (LogQueryCallback callback : logQueryCallbacks)
				result.registerCallback(callback);
			commands.getLast().setNextCommand(result);

			for (LogQueryCommand command : commands)
				command.init();

			logger.trace("kraken logstorage: run query => {}", queryString);
			commands.getFirst().start();
		} catch (Exception e) {
			logger.error("kraken logstorage: cannot start query", e);
		}
	}

	@Override
	public void cancel() {
		if (isEnd())
			return;

		ListIterator<LogQueryCommand> cmdIter = commands.listIterator(commands.size());
		while (cmdIter.hasPrevious())
			cmdIter.previous().eof();

		for (CommandThread thread : resman.getThreads()) {
			if (this.equals(thread.getQuery()))
				thread.interrupt();
		}
	}

	@Override
	public Date getLastStarted() {
		return lastStarted;
	}

	@Override
	public List<Map<String, Object>> getResult() {
		return (result != null) ? result.getResult() : null;
	}

	@Override
	public List<Map<String, Object>> getResult(int offset, int limit) {
		return (result != null) ? result.getResult(offset, limit) : null;
	}

	@Override
	public List<LogQueryCommand> getCommands() {
		return commands;
	}

	@Override
	public Set<LogQueryCallback> getLogQueryCallback() {
		return logQueryCallbacks;
	}

	@Override
	public void registerQueryCallback(LogQueryCallback callback) {
		logQueryCallbacks.add(callback);
	}

	@Override
	public void unregisterQueryCallback(LogQueryCallback callback) {
		logQueryCallbacks.remove(callback);
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

	@Override
	public String toString() {
		return "id=" + id + ", query=" + queryString;
	}
}
