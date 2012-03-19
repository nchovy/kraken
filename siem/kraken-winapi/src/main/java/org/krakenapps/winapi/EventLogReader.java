/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.winapi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventLogReader implements Iterable<EventLog> {
	static {
		System.loadLibrary("winapi");
	}

	private String logName;

	public EventLogReader(String logName) {
		this.logName = logName;
	}

	public String getLogName() {
		return logName;
	}

	private static native EventLog readEventLog(String logName, int offset);

	public List<EventLog> readAllEventLogs() {
		return readAllEventLogs(1);
	}

	public List<EventLog> readAllEventLogs(int begin) {
		return readAllEventLogs(logName, begin);
	}

	private native ArrayList<EventLog> readAllEventLogs(String logName, int begin);

	@Override
	public Iterator<EventLog> iterator() {
		return iterator(1);
	}

	public Iterator<EventLog> iterator(int next) {
		return new EventLogIterator(logName, next);
	}

	private static class EventLogIterator implements Iterator<EventLog> {
		private String logName;
		private EventLog nextLog;
		private int next;

		public EventLogIterator(String logName, int begin) {
			this.logName = logName;
			this.nextLog = null;
			this.next = begin;
		}

		@Override
		public boolean hasNext() {
			if (nextLog != null)
				return true;

			if ((nextLog = readEventLog(logName, next)) != null) {
				next = nextLog.getRecordNumber() + 1;
				return true;
			}

			return false;
		}

		@Override
		public EventLog next() {
			if (hasNext()) {
				EventLog log = nextLog;
				nextLog = null;
				return log;
			}

			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}
}
