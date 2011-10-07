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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.logdb.query.FileBufferMap;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.query.command.Function;

public class Stats extends LogQueryCommand {
	private List<String> clauses;
	private Function[] values;
	private FileBufferMap<List<Object>, List<Function>> result;

	public Stats(List<String> clause, Function[] values) {
		this.clauses = clause;
		this.values = values;
	}

	@Override
	public void init() {
		super.init();
		try {
			result = new FileBufferMap<List<Object>, List<Function>>();
		} catch (IOException e) {
		}

		for (Function f : values)
			f.clean();
	}

	@Override
	public void push(Map<String, Object> m) {
		List<Object> key = new ArrayList<Object>();
		for (String clause : clauses)
			key.add(getData(clause, m));

		if (!result.containsKey(key)) {
			List<Function> fs = new ArrayList<Function>();
			for (Function value : values)
				fs.add(value.clone());
			result.put(key, fs);
		}

		List<Function> fs = result.get(key);
		for (Function f : fs)
			f.put(m);
	}

	@Override
	public boolean isReducer() {
		return true;
	}

	@Override
	public void eof() {
		for (List<Object> key : result.keySet()) {
			Map<String, Object> m = new HashMap<String, Object>();

			for (int i = 0; i < clauses.size(); i++)
				m.put(clauses.get(i), key.get(i));

			List<Function> fs = result.get(key);
			for (int i = 0; i < values.length; i++)
				m.put(values[i].toString(), fs.get(i).getResult());

			write(m);
		}
		super.eof();
		result.close();
		result = null;
	}
}
