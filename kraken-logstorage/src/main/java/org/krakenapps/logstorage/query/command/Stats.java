package org.krakenapps.logstorage.query.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.logstorage.LogQueryCommand;

public class Stats extends LogQueryCommand {
	private List<String> clauses;
	private Function[] values;
	private Map<List<Object>, List<Function>> result = new HashMap<List<Object>, List<Function>>();

	public Stats(List<String> clause, Function[] values) {
		this.clauses = clause;
		this.values = values;
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
		result = null;
		for (Function f : values)
			f.clean();
		super.eof();
	}
}
