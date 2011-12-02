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

import java.util.List;

import org.krakenapps.logdb.LogQueryCommand;

public class Fields extends LogQueryCommand {
	private boolean remove;
	private List<String> fields;

	public Fields(List<String> fields) {
		this(false, fields);
	}

	public Fields(boolean remove, List<String> fields) {
		this.remove = remove;
		this.fields = fields;
	}

	@Override
	public void push(LogMap m) {
		if (remove) {
			for (String field : fields)
				m.remove(field);
		} else {
			LogMap newMap = new LogMap();
			for (String field : fields) {
				Object data = m.get(field);
				newMap.put(field, data);
			}
			m = newMap;
		}
		write(m);
	}

	@Override
	public boolean isReducer() {
		return (remove == fields.contains(headerColumn.get("date")));
	}

	public boolean isRemove() {
		return remove;
	}

	public List<String> getFields() {
		return fields;
	}
}
