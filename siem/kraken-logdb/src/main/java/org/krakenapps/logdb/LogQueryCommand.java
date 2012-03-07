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
package org.krakenapps.logdb;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.krakenapps.logdb.query.FileBufferList;
import org.krakenapps.logdb.query.ResourceManagerImpl.CommandThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LogQueryCommand implements Runnable {
	public static enum Status {
		Waiting, Running, End
	}

	private Logger logger = LoggerFactory.getLogger(LogQueryCommand.class);

	protected ExecutorService exesvc;
	private static final int BULK_SIZE = 1000;
	private BlockingQueue<Object> cache = new LinkedBlockingQueue<Object>();
	private AtomicInteger scheduled = new AtomicInteger();
	private AtomicInteger running = new AtomicInteger();

	private String queryString;
	private AtomicInteger pushCount = new AtomicInteger();
	protected LogQuery logQuery;
	private LogQueryCommand next;
	private boolean callbackTimeline;
	private volatile Status status = Status.Waiting;
	protected Map<String, String> headerColumn = new HashMap<String, String>();

	public LogQueryCommand() {
		// default metadata column mappings
		headerColumn.put("table", "_table");
		headerColumn.put("id", "_id");
		headerColumn.put("date", "_time");
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public void setLogQuery(LogQuery logQuery) {
		this.logQuery = logQuery;
	}

	public void setExecutorService(ExecutorService exesvc) {
		this.exesvc = exesvc;
	}

	public LogQueryCommand getNextCommand() {
		return next;
	}

	public void setNextCommand(LogQueryCommand next) {
		this.next = next;
		this.next.cache = new LinkedBlockingQueue<Object>(getCacheSize());
	}

	public Status getStatus() {
		return status;
	}

	public final void init() {
		this.status = Status.Waiting;
		if (next != null)
			next.headerColumn = this.headerColumn;
		initProcess();
	}

	protected void initProcess() {
	}

	@Override
	public void run() {
		CommandThread cmd = (CommandThread) Thread.currentThread();
		cmd.set(logQuery, this);
		scheduled.decrementAndGet();

		try {
			Queue<Object> q = new LinkedList<Object>();
			cache.drainTo(q, BULK_SIZE);

			try {
				running.incrementAndGet();
				pushCaches(q);
			} finally {
				running.decrementAndGet();
			}
		} catch (Throwable e) {
			logger.error("kraken logdb: log query failed", e);
			logQuery.cancel();
		} finally {
			cmd.unset();
		}
	}

	public int getCacheSize() {
		return Integer.MAX_VALUE;
	}

	private enum Token {
		Start, Eof
	}

	public final void start() {
		status = Status.Running;
		try {
			cache.put(Token.Start);
			exesvc.execute(this);
			scheduled.incrementAndGet();
		} catch (InterruptedException e) {
		}
	}

	protected void startProcess() {
		throw new UnsupportedOperationException();
	}

	public int getPushCount() {
		return pushCount.get();
	}

	@SuppressWarnings("unchecked")
	private void pushCaches(Queue<Object> q) {
		while (!q.isEmpty()) {
			if (status == Status.End)
				return;

			Object poll = q.poll();
			if (poll == Token.Start)
				startProcess();
			else if (poll instanceof LogMap)
				push((LogMap) poll);
			else if (poll instanceof FileBufferList)
				push((FileBufferList<Map<String, Object>>) poll);
			else if (poll == Token.Eof) {
				if (running.get() == 1)
					eof();
				else {
					try {
						cache.put(Token.Eof);
						exesvc.execute(this);
						scheduled.incrementAndGet();
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	public abstract void push(LogMap m);

	public void push(FileBufferList<Map<String, Object>> buf) {
		if (buf != null) {
			for (Map<String, Object> m : buf)
				push(new LogMap(m));
			buf.close();
		}
	}

	protected final void write(LogMap m) {
		int pc = pushCount.incrementAndGet();
		if (next != null && next.status != Status.End) {
			if (callbackTimeline) {
				for (LogTimelineCallback callback : logQuery.getTimelineCallbacks())
					callback.put((Date) m.get(headerColumn.get("date")));
			}
			next.status = Status.Running;
			try {
				next.cache.put(m);
				if (pc % BULK_SIZE == 0) {
					exesvc.execute(next);
					next.scheduled.incrementAndGet();
				}
			} catch (InterruptedException e) {
			}
		}
	}

	protected final void write(FileBufferList<Map<String, Object>> buf) {
		pushCount.addAndGet(buf.size());
		if (next != null && next.status != Status.End) {
			next.status = Status.Running;
			try {
				next.cache.put(buf);
				exesvc.execute(next);
				next.scheduled.incrementAndGet();
			} catch (InterruptedException e) {
			}
		} else {
			buf.close();
		}
	}

	public abstract boolean isReducer();

	public boolean isCallbackTimeline() {
		return callbackTimeline;
	}

	public void setCallbackTimeline(boolean callbackTimeline) {
		this.callbackTimeline = callbackTimeline;
	}

	public final void eof() {
		if (status == Status.End)
			return;

		status = Status.End;
		eofProcess();

		if (next != null && next.status != Status.End) {
			exesvc.execute(next);
			next.scheduled.incrementAndGet();
			try {
				next.cache.put(Token.Eof);
				exesvc.execute(next);
				next.scheduled.incrementAndGet();
			} catch (InterruptedException e) {
			}
		}

		if (logQuery != null) {
			if (callbackTimeline) {
				for (LogTimelineCallback callback : logQuery.getTimelineCallbacks())
					callback.callback();
				logQuery.getTimelineCallbacks().clear();
			}
			if (logQuery.getCommands().get(0).status != Status.End)
				logQuery.getCommands().get(0).eof();
		}
	}

	protected void eofProcess() {
	}

	public static class LogMap {
		private Map<String, Object> map;

		public LogMap() {
			this(new HashMap<String, Object>());
		}

		public LogMap(Map<String, Object> map) {
			this.map = map;
		}

		public Object get(String key) {
			return get(map, key);
		}

		@SuppressWarnings("unchecked")
		private Object get(Map<String, Object> m, String key) {
			if (key == null)
				return null;

			if (!key.endsWith("]") || !key.contains("["))
				return m.get(key);

			int begin = key.indexOf("[");
			String thisKey = key.substring(0, begin);
			if (map.containsKey(thisKey) && (map.get(thisKey) instanceof Map)) {
				int end = key.lastIndexOf("]");
				return get((Map<String, Object>) map.get(thisKey), key.substring(begin + 1, end));
			} else
				return m.get(key);
		}

		public void put(String key, Object value) {
			map.put(key, value);
		}

		public void putAll(Map<? extends String, ? extends Object> m) {
			map.putAll(m);
		}

		public Object remove(String key) {
			return map.remove(key);
		}

		public boolean containsKey(String key) {
			return map.containsKey(key);
		}

		public Map<String, Object> map() {
			return map;
		}
	}
}
