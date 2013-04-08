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
import java.util.Map;

import org.krakenapps.logdb.query.FileBufferList;

public abstract class LogQueryCommand {
	public static enum Status {
		Waiting, Running, End, Finalizing
	}

	private String queryString;
	private int pushCount;
	protected LogQuery logQuery;
	protected LogQueryCommand next;
	private boolean callbackTimeline;
	protected volatile Status status = Status.Waiting;
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

	public LogQueryCommand getNextCommand() {
		return next;
	}

	public void setNextCommand(LogQueryCommand next) {
		this.next = next;
	}

	public Status getStatus() {
		return status;
	}

	public void init() {
		this.status = Status.Waiting;
		if (next != null)
			next.headerColumn = this.headerColumn;
	}

	public void start() {
		throw new UnsupportedOperationException();
	}

	public int getPushCount() {
		return pushCount;
	}

	public abstract void push(LogMap m);

	protected final void write(LogMap m) {
		pushCount++;
		if (next != null && next.status != Status.End) {
			if (callbackTimeline) {
				for (LogTimelineCallback callback : logQuery.getTimelineCallbacks())
					callback.put((Date) m.get(headerColumn.get("date")));
			}
			next.status = Status.Running;
			next.push(m);
		}
	}

	@Deprecated
	public void push(FileBufferList<Map<String, Object>> buf) {
		if (buf != null) {
			for (Map<String, Object> m : buf)
				push(new LogMap(m));
			buf.close();
		}
	}

	@Deprecated
	protected final void write(FileBufferList<Map<String, Object>> buf) {
		pushCount += buf.size();
		if (next != null && next.status != Status.End) {
			next.status = Status.Running;
			next.push(buf);
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

	public void eof() {
		status = Status.End;

		if (next != null && next.status != Status.End && next.status != Status.Finalizing)
			next.eof();

		if (logQuery != null) {
			if (callbackTimeline) {
				for (LogTimelineCallback callback : logQuery.getTimelineCallbacks())
					callback.eof();
				logQuery.clearTimelineCallbacks();
			}
			if (logQuery.getCommands().get(0).status != Status.End)
				logQuery.getCommands().get(0).eof();
		}
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

		public void remove(String key) {
			map.remove(key);
		}

		public boolean containsKey(String key) {
			return map.containsKey(key);
		}

		public Map<String, Object> map() {
			return map;
		}
	}
}
