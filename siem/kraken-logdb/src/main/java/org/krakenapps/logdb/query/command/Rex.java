/*
 * Copyright 2013 Future Systems
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.krakenapps.logdb.LogQueryCommand;

public class Rex extends LogQueryCommand {

	private String field;
	private Pattern p;
	private String[] names;

	public Rex(String field, Pattern p, String[] names) {
		this.field = field;
		this.p = p;
		this.names = names;
	}

	public String getInputField() {
		return field;
	}

	public Pattern getPattern() {
		return p;
	}

	public String[] getOutputNames() {
		return names;
	}

	@Override
	public void push(LogMap m) {
		Object o = m.get(field);
		if (o == null) {
			write(m);
			return;
		}

		String s = o.toString();

		Matcher matcher = p.matcher(s);
		if (matcher.find())
			for (int i = 0; i < matcher.groupCount(); i++)
				m.put(names[i], matcher.group(i + 1));

		write(m);
	}

	@Override
	public boolean isReducer() {
		return false;
	}

}
