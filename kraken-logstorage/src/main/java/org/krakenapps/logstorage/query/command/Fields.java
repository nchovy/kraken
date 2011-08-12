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
				if (m.containsKey(field))
					newMap.put(field, m.get(field));
			}

			m = newMap;
			m.put("_fields", fields);
		}

		write(m);
	}
}
