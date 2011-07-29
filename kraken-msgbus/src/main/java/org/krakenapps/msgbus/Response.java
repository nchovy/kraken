package org.krakenapps.msgbus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Response implements Map<String, Object> {

	private Map<String, Object> m;

	public Response() {
		m = new HashMap<String, Object>();
	}

	@Override
	public void clear() {
		m.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return m.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return m.containsKey(value);
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return m.entrySet();
	}

	@Override
	public Object get(Object key) {
		return m.get(key);
	}

	@Override
	public boolean isEmpty() {
		return m.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return m.keySet();
	}

	@Override
	public Object put(String key, Object value) {
		return m.put(key, convert(value));
	}

	private Object convert(Object value) {
		if (value instanceof Date) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
			return sdf.format((Date) value);
		} else if (value instanceof Map) {
			Map<?, ?> m = (Map<?, ?>) value;
			Map<Object, Object> mm = new HashMap<Object, Object>();
			for (Object key : m.keySet())
				mm.put(convert(key), convert(m.get(key)));
			return mm;
		} else if (value instanceof Collection) {
			return convertList((Collection<?>) value);
		} else if (value.getClass().isArray()) {
			return convertList(Arrays.asList((Object[]) value));
		}

		return value;
	}

	private Object convertList(Collection<?> value) {
		List<Object> list = new ArrayList<Object>();
		for (Object obj : value)
			list.add(convert(obj));
		return list;
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		for (String key : m.keySet())
			put(key, m.get(key));
	}

	@Override
	public Object remove(Object key) {
		return m.remove(key);
	}

	@Override
	public int size() {
		return m.size();
	}

	@Override
	public Collection<Object> values() {
		return m.values();
	}
}
