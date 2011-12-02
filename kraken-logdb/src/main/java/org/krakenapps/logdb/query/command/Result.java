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
package org.krakenapps.logdb.query.command;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.krakenapps.logdb.LogQueryCallback;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.query.FileBufferList;

public class Result extends LogQueryCommand {
	private FileBufferList<Map<String, Object>> result;
	private Set<LogQueryCallback> callbacks;
	private Queue<LogQueryCallbackInfo> callbackQueue;
	private Integer nextCallback;

	public Result() throws IOException {
		result = new FileBufferList<Map<String, Object>>();
		callbacks = new HashSet<LogQueryCallback>();
		callbackQueue = new PriorityQueue<Result.LogQueryCallbackInfo>(11, new CallbackInfoComparator());
	}

	private class LogQueryCallbackInfo {
		private int size;
		private LogQueryCallback callback;

		public LogQueryCallbackInfo(LogQueryCallback callback) {
			this.size = callback.offset() + callback.limit();
			this.callback = callback;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((callback == null) ? 0 : callback.hashCode());
			result = prime * result + size;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LogQueryCallbackInfo other = (LogQueryCallbackInfo) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (callback == null) {
				if (other.callback != null)
					return false;
			} else if (!callback.equals(other.callback))
				return false;
			if (size != other.size)
				return false;
			return true;
		}

		private Result getOuterType() {
			return Result.this;
		}
	}

	private class CallbackInfoComparator implements Comparator<LogQueryCallbackInfo> {
		@Override
		public int compare(LogQueryCallbackInfo o1, LogQueryCallbackInfo o2) {
			return o1.size - o2.size;
		}
	}

	@Override
	public void push(LogMap m) {
		result.add(m.map());

		while (nextCallback != null && result.size() >= nextCallback) {
			LogQueryCallback callback = callbackQueue.poll().callback;
			callback.onPageLoaded(result);
			if (callbackQueue.isEmpty())
				nextCallback = null;
			else
				nextCallback = callbackQueue.peek().size;
		}
	}

	@Override
	public boolean isReducer() {
		return false;
	}

	public void registerCallback(LogQueryCallback callback) {
		callbacks.add(callback);
		callbackQueue.add(new LogQueryCallbackInfo(callback));
		nextCallback = this.callbackQueue.peek().size;
	}

	public void unregisterCallback(LogQueryCallback callback) {
		callbacks.remove(callback);
		callbackQueue.remove(new LogQueryCallbackInfo(callback));
		nextCallback = null;
		if (!this.callbackQueue.isEmpty())
			nextCallback = this.callbackQueue.peek().size;
	}

	public FileBufferList<Map<String, Object>> getResult() {
		return result;
	}

	public List<Map<String, Object>> getResult(int offset, int limit) {
		if (offset + limit > result.size())
			limit = result.size() - offset;
		return result.subList(offset, offset + limit);
	}

	@Override
	public void eof() {
		super.eof();
		for (LogQueryCallback callback : callbacks)
			callback.onEof();
	}
}
