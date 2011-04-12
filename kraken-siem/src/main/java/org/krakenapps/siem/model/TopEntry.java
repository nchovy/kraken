package org.krakenapps.siem.model;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.msgbus.Marshalable;

// used by jpa select new query
public class TopEntry implements Marshalable {
	private String item;
	private long count;

	public TopEntry(String item, long count) {
		this.item = item;
		this.count = count;
	}

	@Override
	public Map<String, Object> marshal() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("item", item);
		m.put("count", count);
		return m;
	}
}