/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.eventstorage.engine;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.eventstorage.Event;

public class EventReadHelper {
	private final int offset;
	private final int limit;
	private int hits = 0;
	private LongSet readed = new LongSet();
	private List<Event> result = new ArrayList<Event>();

	public EventReadHelper(int offset, int limit) {
		this.offset = offset;
		this.limit = limit;
	}

	public int getOffset() {
		return offset;
	}

	public int getLimit() {
		return limit;
	}

	public int getHits() {
		return hits;
	}

	public int incrementHits(int n) {
		hits += n;
		return hits;
	}

	public boolean addReadedId(long id) {
		return readed.add(id);
	}

	public List<Event> getResult() {
		return result;
	}

	public void addResult(Event event) {
		result.add(event);
	}

	public void close() {
		if (readed != null)
			readed.close();
	}
}
