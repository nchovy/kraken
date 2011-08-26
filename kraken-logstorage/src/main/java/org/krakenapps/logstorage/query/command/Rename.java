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

import java.util.Map;

import org.krakenapps.logstorage.LogQueryCommand;

public class Rename extends LogQueryCommand {
	private String from;
	private String to;

	public Rename(String from, String to) {
		this.from = from;
		this.to = to;
	}

	@Override
	protected String getDateColumnName() {
		if (from.equals(dateColumnName))
			return to;
		return super.getDateColumnName();
	}

	@Override
	public void setDataHeader(String[] header) {
		for (int i = 0; i < header.length; i++) {
			if (header[i].equals(from))
				header[i] = to;
		}

		super.setDataHeader(header);
	}

	@Override
	public void push(Map<String, Object> m) {
		if (m.containsKey(from)) {
			m.put(to, m.get(from));
			m.remove(from);
		}
		write(m);
	}

	@Override
	public boolean isReducer() {
		return false;
	}
}
