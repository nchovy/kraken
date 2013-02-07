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
package org.krakenapps.siem.model;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.msgbus.Marshalable;

// used by jpa select new query
public class TopEntry implements Marshalable {
	private String item;
	private long count;

	public TopEntry(String item, long count) {
		this.item = item;
		this.count = count;
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("item", item);
		m.put("count", count);
		return m;
	}
}