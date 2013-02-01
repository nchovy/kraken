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
	private List<String> fields;
	private boolean selector;

	public Fields(List<String> fields) {
		this(fields, true);
	}

	public Fields(List<String> fields, boolean selector) {
		this.fields = fields;
		this.selector = selector;
	}

	@Override
	public void push(LogMap m) {
		if (selector) {
			LogMap newMap = new LogMap();
			for (String field : fields) {
				Object data = m.get(field);
				newMap.put(field, data);
			}
			m = newMap;
		} else {
			for (String field : fields)
				m.remove(field);
		}
		write(m);
	}

	@Override
	public boolean isReducer() {
		return false;
	}

	public List<String> getFields() {
		return fields;
	}

	public boolean isSelector() {
		return selector;
	}
}
