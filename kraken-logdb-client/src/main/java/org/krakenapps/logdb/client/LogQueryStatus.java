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
package org.krakenapps.logdb.client;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class LogQueryStatus {
	private int id;
	private String query;
	private boolean isRunning;
	private boolean ended;
	private CopyOnWriteArraySet<LogQueryCallback> callbacks;

	public LogQueryStatus(int id, String query, boolean isRunning) {
		this.id = id;
		this.query = query;
		this.isRunning = isRunning;
		this.callbacks = new CopyOnWriteArraySet<LogQueryCallback>();
	}

	public int getId() {
		return id;
	}

	public String getQuery() {
		return query;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public boolean isEnded() {
		return ended;
	}

	public void setEnded(boolean ended) {
		this.ended = ended;
	}

	public Set<LogQueryCallback> getCallbacks() {
		return Collections.unmodifiableSet(callbacks);
	}

	public void addCallback(LogQueryCallback callback) {
		callbacks.add(callback);
	}

	public void removeCallback(LogQueryCallback callback) {
		callbacks.remove(callback);
	}

	@Override
	public String toString() {
		return "id=" + id + ", query=[" + query + "], running=" + isRunning;
	}
}
