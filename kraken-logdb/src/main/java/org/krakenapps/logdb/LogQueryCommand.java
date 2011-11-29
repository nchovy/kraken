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
import java.util.Map;

import org.krakenapps.logdb.query.FileBufferList;

public abstract class LogQueryCommand {
	public static enum Status {
		Waiting, Running, End
	}

	private String queryString;
	private int pushCount;
	protected LogQuery logQuery;
	protected String dateColumnName;
	protected LogQueryCommand next;
	private boolean callbackTimeline;
	protected volatile Status status = Status.Waiting;
	private Map<String, Object> parsed = null;

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public void setLogQuery(LogQuery logQuery) {
		this.logQuery = logQuery;
	}

	protected String getDateColumnName() {
		if (isReducer())
			return null;
		return dateColumnName;
	}

	protected void setDateColumnName(String dateColumnName) {
		this.dateColumnName = dateColumnName;
	}

	protected Object getData(String key, Map<String, Object> m) {
		if (m.containsKey(key))
			return m.get(key);
		else if (parsed == null)
			parsed = logQuery.getLogParser().parse(m);

		return parsed.get(key);
	}

	public LogQueryCommand getNextCommand() {
		return next;
	}

	public void setNextCommand(LogQueryCommand next) {
		this.next = next;
		setNextCommandConfigure();
	}

	private void setNextCommandConfigure() {
		if (next != null) {
			next.setDateColumnName(getDateColumnName());
			next.setNextCommandConfigure();
		}
	}

	public Status getStatus() {
		return status;
	}

	public void init() {
		this.status = Status.Waiting;
	}

	public void start() {
		throw new UnsupportedOperationException();
	}

	public int getPushCount() {
		return pushCount;
	}

	public abstract void push(Map<String, Object> m);

	public void push(FileBufferList<Map<String, Object>> buf) {
		if (buf != null) {
			for (Map<String, Object> m : buf)
				push(m);
			buf.close();
		}
	}

	protected final void write(Map<String, Object> m) {
		pushCount++;
		if (next != null && next.status != Status.End) {
			if (callbackTimeline) {
				for (LogTimelineCallback callback : logQuery.getTimelineCallbacks())
					callback.put((Date) m.get(dateColumnName));
			}
			next.status = Status.Running;
			next.push(m);
		}
	}

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

		if (next != null && next.status != Status.End)
			next.eof();

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
}
