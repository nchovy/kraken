package org.krakenapps.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrimitiveConverter {
	public static Object serialize(Collection<?> c) {
		List<Object> l = new ArrayList<Object>();
		for (Object o : c)
			l.add(serialize(o));

		return l;
	}

	public static Object serialize(Object o) {
		Map<String, Object> m = new HashMap<String, Object>();
		Class<?> c = o.getClass();

		for (Field f : c.getDeclaredFields()) {
			try {
				String fieldName = toUnderscoreName(f.getName());
				f.setAccessible(true);
				Object value = f.get(o);

				if (value instanceof Enum)
					m.put(fieldName, value.toString());
				else if (value instanceof List) {
					List<?> l = (List<?>) value;
					List<Object> l2 = new ArrayList<Object>(l.size());
					for (Object el : l)
						l2.add(serialize(el));

					m.put(fieldName, l2);
				} else
					m.put(fieldName, value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return m;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> parse(Class<T> clazz, List<?> c) {
		List<T> l = new ArrayList<T>();
		for (Object o : c)
			l.add(parse(clazz, (Map<String, Object>) o));

		return l;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T parse(Class<T> clazz, Map<String, Object> m) {
		try {
			Constructor c = clazz.getConstructor();
			c.setAccessible(true);
			T n = (T) c.newInstance();

			for (Field f : clazz.getDeclaredFields()) {
				CollectionTypeHint hint = f.getAnnotation(CollectionTypeHint.class);
				Class<?> fieldType = f.getType();
				String fieldName = toUnderscoreName(f.getName());

				Object value = m.get(fieldName);
				f.setAccessible(true);

				if (fieldType.isEnum() && value instanceof String) {
					Object found = null;
					for (Object o : fieldType.getEnumConstants())
						if (o.toString().equals(value))
							found = o;

					f.set(n, found);
				} else if (contains(fieldType, List.class)) {
					if (value instanceof Object[])
						value = Arrays.asList((Object[]) value);

					if (value instanceof List) {
						if (hint != null)
							f.set(n, parseList(hint.value(), (List) value));
						else
							f.set(n, value);
					}
				} else if (!fieldType.isInstance(Map.class) && value instanceof Map) {
					f.set(n, parse(fieldType, (Map) value));
				} else {
					f.set(n, value);
				}
			}

			return n;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static boolean contains(Class<?> clazz, Class<?> iface) {
		if (clazz.equals(iface))
			return true;

		Class<?>[] clazzes = clazz.getInterfaces();
		for (int i = 0; i < clazzes.length; i++)
			if (clazzes[i].equals(iface))
				return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> parseList(Class<T> clazz, List<Object> l) {
		List<T> list = new ArrayList<T>();
		for (Object o : l) {
			if (o instanceof Map) {
				T v = parse(clazz, (Map<String, Object>) o);
				list.add(v);
			} else
				list.add((T) o);
		}

		return list;
	}

	public static String toUnderscoreName(String s) {
		final int tolower = 'a' - 'A';
		StringBuilder sb = new StringBuilder(s.length() * 2);

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 'A' && c <= 'Z') {
				if (i != 0)
					sb.append("_");

				sb.append((char) (c + tolower));
			} else
				sb.append(c);
		}

		return sb.toString();
	}

}
