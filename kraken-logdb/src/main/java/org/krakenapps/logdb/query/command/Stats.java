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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.query.FileBufferList;
import org.krakenapps.logdb.query.ObjectComparator;

public class Stats extends LogQueryCommand {
	private List<String> clauses;
	private Function[] values;
	private FileBufferList<Map<String, Object>> list;
	private Set<String> keyFilter = new HashSet<String>();

	private Comparator<Map<String, Object>> cmp = new Comparator<Map<String, Object>>() {
		private Comparator<Object> cmp = new ObjectComparator();

		@Override
		public int compare(Map<String, Object> m1, Map<String, Object> m2) {
			for (String clause : clauses) {
				int c = cmp.compare(m1.get(clause), m2.get(clause));
				if (c != 0)
					return c;
			}
			return 0;
		}
	};

	public Stats(List<String> clause, Function[] values) {
		this.clauses = clause;
		this.values = values;

		for (Function func : values) {
			if (func.getTarget() != null)
				keyFilter.add(func.getTarget());
		}
		keyFilter.addAll(clauses);
	}

	@Override
	protected void initProcess() {
		try {
			list = new FileBufferList<Map<String, Object>>(cmp);
		} catch (IOException e) {
		}

		for (Function f : values)
			f.clean();
	}

	@Override
	public void push(LogMap m) {
		Map<String, Object> map = new HashMap<String, Object>();
		for (Entry<String, Object> e : m.map().entrySet()) {
			if (keyFilter.contains(e.getKey()))
				map.put(e.getKey(), e.getValue());
		}
		list.add(map);
	}

	@Override
	public boolean isReducer() {
		return true;
	}

	@Override
	protected void eofProcess() {
		Function[] funcs = new Function[values.length];

		Map<String, Object> before = null;
		for (Map<String, Object> m : list) {
			if (before == null || cmp.compare(before, m) != 0) {
				if (before != null) {
					LogMap result = new LogMap();
					for (String clause : clauses)
						result.put(clause, before.get(clause));
					for (Function func : funcs)
						result.put(func.toString(), func.getResult());
					write(result);
				}

				for (int i = 0; i < values.length; i++)
					funcs[i] = values[i].clone();
			}

			for (Function func : funcs)
				func.put(new LogMap(m));

			before = m;
		}

		Map<String, Object> result = new HashMap<String, Object>();
		for (String clause : clauses)
			result.put(clause, before.get(clause));
		for (Function func : funcs)
			result.put(func.toString(), func.getResult());
		write(new LogMap(result));

		list.close();
		list = null;
	}
}
