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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryCallback;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogQueryCommand.Status;
import org.krakenapps.logdb.LogResultSet;
import org.krakenapps.logdb.LogTimelineCallback;
import org.krakenapps.logdb.SyntaxProvider;
import org.krakenapps.logdb.query.command.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogQueryImpl implements LogQuery {
	private Logger logger = LoggerFactory.getLogger(LogQueryImpl.class);
	private static AtomicInteger nextId = new AtomicInteger(1);

	private final int id = nextId.getAndIncrement();
	private String queryString;
	private List<LogQueryCommand> commands = new ArrayList<LogQueryCommand>();
	private Date lastStarted;
	private Result result;
	private Set<LogQueryCallback> logQueryCallbacks = new CopyOnWriteArraySet<LogQueryCallback>();
	private Set<LogTimelineCallback> timelineCallbacks = new CopyOnWriteArraySet<LogTimelineCallback>();

	public LogQueryImpl(SyntaxProvider syntaxProvider, String queryString) {
		this.queryString = queryString;

		for (String q : split(queryString)) {
			q = q.trim();
			try {
				LogQueryCommand cmd = syntaxProvider.eval(this, q);
				commands.add(cmd);
			} catch (ParseException e) {
				throw new IllegalArgumentException("invalid query command: " + q);
			}
		}

		if (commands.isEmpty())
			throw new IllegalArgumentException("empty query");

		boolean setReducer = false;
		for (int i = 0; i < commands.size(); i++) {
			LogQueryCommand command = commands.get(i);
			if (i < commands.size() - 1)
				command.setNextCommand(commands.get(i + 1));
			if (command.isReducer() && !setReducer && i > 0) {
				setReducer = true;
				commands.get(i - 1).setCallbackTimeline(true);
			}
		}
		if (!setReducer)
			commands.get(commands.size() - 1).setCallbackTimeline(true);
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
	public void run() {
		if (!isEnd())
			throw new IllegalStateException("already running");

		lastStarted = new Date();
		if (commands.isEmpty())
			return;

		try {
			result = new Result();
			result.setLogQuery(this);
			commands.get(commands.size() - 1).setNextCommand(result);
			for (LogQueryCallback callback : logQueryCallbacks)
				result.registerCallback(callback);
			logQueryCallbacks.clear();

			logger.trace("kraken logdb: run query => {}", queryString);
			for (LogQueryCommand command : commands)
				command.init();

			commands.get(0).start();
		} catch (Exception e) {
			logger.error("kraken logdb: query failed - " + this, e);
		}
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
		if (commands.get(0).getStatus() == Status.Waiting)
			return true;
		return result.getStatus().equals(Status.End);
	}

	@Override
	public void purge() {
		// prevent deleted result file access caused by result check of query
		// callback or timeline callbacks
		clearTimelineCallbacks();
		clearQueryCallbacks();

		if (result != null)
			result.purge();
	}

	@Override
	public void cancel() {
		if (result == null)
			return;

		if (result.getStatus() != Status.End && result.getStatus() != Status.Finalizing)
			result.eof();

		for (int i = commands.size() - 1; i >= 0; i--) {
			LogQueryCommand command = commands.get(i);
			if (command.getStatus() != Status.End && command.getStatus() != Status.Finalizing) {
				command.eof();
			}
		}
	}

	@Override
	public Date getLastStarted() {
		return lastStarted;
	}

	@Override
	public LogResultSet getResult() throws IOException {
		if (result != null)
			return result.getResult();
		return null;
	}

	@Override
	public Long getResultCount() throws IOException {
		if (result == null)
			return null;

		LogResultSet rs = null;
		try {
			rs = result.getResult();
			return rs.size();
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public List<Map<String, Object>> getResultAsList() throws IOException {
		return getResultAsList(0, Integer.MAX_VALUE);
	}

	@Override
	public List<Map<String, Object>> getResultAsList(long offset, int limit) throws IOException {
		LinkedList<Map<String, Object>> l = new LinkedList<Map<String, Object>>();

		LogResultSet rs = getResult();
		if (rs == null)
			return null;

		try {
			long p = 0;
			long count = 0;
			while (rs.hasNext()) {
				if (count >= limit)
					break;

				Map<String, Object> m = rs.next();
				if (p++ < offset)
					continue;

				l.add(m);
				count++;
			}
		} finally {
			rs.close();
		}
		return l;
	}

	@Override
	public List<LogQueryCommand> getCommands() {
		return commands;
	}

	@Override
	public Set<LogQueryCallback> getLogQueryCallback() {
		return Collections.unmodifiableSet(logQueryCallbacks);
	}

	@Override
	public void registerQueryCallback(LogQueryCallback callback) {
		logQueryCallbacks.add(callback);
	}

	@Override
	public void unregisterQueryCallback(LogQueryCallback callback) {
		logQueryCallbacks.add(callback);
	}

	@Override
	public void clearQueryCallbacks() {
		logQueryCallbacks.clear();
	}

	@Override
	public Set<LogTimelineCallback> getTimelineCallbacks() {
		return Collections.unmodifiableSet(timelineCallbacks);
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
	public void clearTimelineCallbacks() {
		timelineCallbacks.clear();
	}

	@Override
	public String toString() {
		return "id=" + id + ", query=" + queryString;
	}

}
