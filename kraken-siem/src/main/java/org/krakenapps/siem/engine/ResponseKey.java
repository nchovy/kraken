/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.siem.engine;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.msgbus.Marshalable;

public class ResponseKey implements Marshalable {
	private String category;
	private int eventSource;

	public ResponseKey(String category) {
		this(category, 0);
	}

	public ResponseKey(String category, int eventSource) {
		this.category = category;
		this.eventSource = eventSource;
	}

	public String getCategory() {
		return category;
	}

	public int getEventSource() {
		return eventSource;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + eventSource;
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
		ResponseKey other = (ResponseKey) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (eventSource != other.eventSource)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("category=%s, event source=%d", category, eventSource);
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("category", category);
		m.put("evt_source", eventSource);
		return m;
	}
}
