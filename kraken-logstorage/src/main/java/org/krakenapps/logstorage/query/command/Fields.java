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
package org.krakenapps.logstorage.query.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.logstorage.LogQueryCommand;

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
	public void push(Map<String, Object> m) {
		if (remove) {
			for (String field : fields)
				m.remove(field);
		} else {
			Map<String, Object> newMap = new HashMap<String, Object>();
			for (String field : fields) {
				Object data = getData(field, m);
				if (data != null)
					newMap.put(field, data);
			}

			m = newMap;
			m.put("_fields", fields);
		}

		write(m);
	}

	@Override
	public boolean isReducer() {
		return (remove == fields.contains(dateColumnName));
	}
}
