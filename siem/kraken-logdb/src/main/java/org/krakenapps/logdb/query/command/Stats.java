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

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.query.FileBufferMap;

public class Stats extends LogQueryCommand {
	private List<String> clauses;
	private Function[] values;
	private FileBufferMap<List<Object>, Function[]> result;

	public Stats(List<String> clause, Function[] values) {
		this.clauses = clause;
		this.values = values;
	}

	@Override
	public void init() {
		super.init();
		try {
			result = new FileBufferMap<List<Object>, Function[]>(FunctionCodec.instance);
		} catch (IOException e) {
		}

		for (Function f : values)
			f.clean();
	}

	@Override
	public void push(LogMap m) {
		List<Object> key = new ArrayList<Object>();
		for (String clause : clauses) {
			Object keyValue = m.get(clause);
			if (keyValue == null)
				return;

			key.add(keyValue);
		}

		if (!result.containsKey(key)) {
			Function[] fs = new Function[values.length];
			for (int i = 0; i < fs.length; i++)
				fs[i] = values[i].clone();
			result.put(key, fs);
		}

		Function[] fs = result.get(key);
		for (Function f : fs)
			f.put(m);
	}

	@Override
	public boolean isReducer() {
		return true;
	}

	@Override
	public void eof() {
		this.status = Status.Finalizing;
		result.flush();

		for (List<Object> key : result.keySet()) {
			Map<String, Object> m = new HashMap<String, Object>();

			for (int i = 0; i < clauses.size(); i++)
				m.put(clauses.get(i), key.get(i));

			Function[] fs = result.get(key);
			for (int i = 0; i < values.length; i++)
				m.put(values[i].toString(), fs[i].getResult());

			write(new LogMap(m));
		}
		super.eof();
		result.close();
		result = null;
	}
}
