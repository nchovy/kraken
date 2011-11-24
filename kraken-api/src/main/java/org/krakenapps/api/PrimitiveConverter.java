package org.krakenapps.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PrimitiveConverter {
	@SuppressWarnings("unchecked")
	private static Set<Class<?>> nonSerializeClasses = new HashSet<Class<?>>(Arrays.asList(byte.class, Byte.class, short.class,
			Short.class, int.class, Integer.class, long.class, Long.class, float.class, Float.class, double.class, Double.class,
			boolean.class, Boolean.class, char.class, Character.class, String.class, Date.class));

	public static Object serialize(Object obj) {
		return serialize(obj, obj, null, null);
	}

	public static Object serialize(Object obj, PrimitiveSerializeCallback callback) {
		return serialize(obj, obj, callback, null);
	}

	@SuppressWarnings("unchecked")
	private static Object serialize(Object root, Object obj, PrimitiveSerializeCallback callback, List<String> refkey) {
		if (obj == null)
			return null;

		Class<?> cls = obj.getClass();

		// Primitive Object
		if (nonSerializeClasses.contains(cls))
			return obj;

		// Collection
		if (Collection.class.isAssignableFrom(cls)) {
			Collection<Object> col = new ArrayList<Object>();
			for (Object o : (Collection<Object>) obj)
				col.add(serialize(root, o, callback, refkey));
			return col;
		}

		// Map
		if (Map.class.isAssignableFrom(cls)) {
			Map<Object, Object> m = new HashMap<Object, Object>();
			Map<Object, Object> o = (Map<Object, Object>) obj;
			for (Object key : o.keySet()) {
				Object k = serialize(root, key, callback, refkey);
				Object value = o.get(key);
				Object v = serialize(root, value, callback, refkey);
				m.put(k, v);
			}
			return m;
		}

		// Others
		Map<String, Object> m = new HashMap<String, Object>();
		for (Field f : cls.getDeclaredFields()) {
			try {
				FieldOption option = f.getAnnotation(FieldOption.class);

				if (option != null && option.skip())
					continue;

				if (refkey != null && !refkey.contains(f.getName()))
					continue;

				String fieldName = toUnderscoreName(f.getName());
				if (option != null && !option.name().isEmpty())
					fieldName = option.name();

				f.setAccessible(true);
				Object value = f.get(obj);

				if (option != null && !option.nullable() && value == null)
					throw new IllegalArgumentException(String.format("Can not set %s field %s.%s to null value", f.getType()
							.getSimpleName(), cls.getName(), f.getName()));

				if (value instanceof Enum) {
					m.put(fieldName, value.toString());
				} else if (value instanceof String) {
					if (option != null && option.length() > 0 && ((String) value).length() > option.length())
						throw new IllegalArgumentException(String.format("String field %s.%s too long", cls.getName(), f.getName()));
					m.put(fieldName, value);
				} else {
					if (value == null)
						m.put(fieldName, null);
					else {
						List<String> referenceKey = null;
						if (f.getAnnotation(ReferenceKey.class) != null && callback != null)
							referenceKey = Arrays.asList(f.getAnnotation(ReferenceKey.class).value());
						Object serialized = serialize(root, value, callback, referenceKey);
						m.put(fieldName, serialized);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("kraken api: serialize failed", e);
			}
		}
		if (callback != null && refkey != null)
			callback.onSerialize(root, cls, obj, m);

		return m;
	}

	public static <T> T parse(Class<T> cls, Object obj) {
		return parse(cls, obj, null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T parse(Class<T> cls, Object obj, PrimitiveParseCallback callback) {
		if (!(obj instanceof Map) || cls.equals(Object.class))
			return (T) obj;

		try {
			Map<String, Object> m = (Map<String, Object>) obj;

			T n = null;
			try {
				n = cls.newInstance();
			} catch (IllegalAccessException e) {
				Constructor c = cls.getConstructor();
				c.setAccessible(true);
				n = (T) c.newInstance();
			}

			for (Field f : cls.getDeclaredFields()) {
				FieldOption option = f.getAnnotation(FieldOption.class);

				if (option != null && option.skip())
					continue;

				String fieldName = toUnderscoreName(f.getName());
				if (option != null && !option.name().isEmpty())
					fieldName = option.name();

				if (option != null && !option.nullable()) {
					if (!m.containsKey(fieldName) || m.get(fieldName) == null)
						throw new IllegalArgumentException(fieldName + " cannot be null.");
				}

				Object value = m.get(fieldName);
				f.setAccessible(true);
				if (value == null) {
					f.set(n, null);
					continue;
				}

				Class<?> fieldType = f.getType();
				ReferenceKey refkey = f.getAnnotation(ReferenceKey.class);
				if (refkey != null) {
					if (value instanceof Map) {
						Map<String, Object> keys = (Map<String, Object>) value;
						if (callback != null)
							f.set(n, callback.onParse(fieldType, keys));
						else if (option != null && !option.nullable())
							throw new IllegalArgumentException(fieldName + " requires parse callback");
						continue;
					} else if (value instanceof Object[]) {
						List<Object> coll = new ArrayList<Object>();
						for (Object v : (Object[]) value) {
							Map<String, Object> keys = (Map<String, Object>) v;
							if (callback != null) {
								coll.add(callback.onParse(f.getAnnotation(CollectionTypeHint.class).value(), keys));
							} else if (option != null && !option.nullable())
								throw new IllegalArgumentException(fieldName + " requires parse callback");
						}

						// TODO
						if (List.class.isAssignableFrom(fieldType))
							f.set(n, coll);
						else if (Set.class.isAssignableFrom(fieldType))
							f.set(n, new HashSet<Object>(coll));
						else if (fieldType.isArray())
							f.set(n, coll.toArray());
						continue;
					}
				}

				if (fieldType.isEnum()) {
					Object found = null;
					for (Object o : fieldType.getEnumConstants())
						if (o.toString().equals(value))
							found = o;
					f.set(n, found);
				} else if (Collection.class.isAssignableFrom(fieldType)) {
					if (value instanceof Object[])
						value = Arrays.asList((Object[]) value);

					CollectionTypeHint hint = f.getAnnotation(CollectionTypeHint.class);
					if (value instanceof List) {
						if (hint != null)
							f.set(n, parseCollection(hint.value(), (List) value));
						else
							f.set(n, value);
					}
				} else if (Map.class.isAssignableFrom(fieldType)) {
					MapTypeHint hint = f.getAnnotation(MapTypeHint.class);
					if (hint == null) {
						f.set(n, value);
					} else {
						Map<Object, Object> v = (Map<Object, Object>) value;
						Map<Object, Object> fmap = new HashMap<Object, Object>();
						for (Object k : v.keySet())
							fmap.put(parse(hint.value()[0], k, callback), parse(hint.value()[1], v.get(k), callback));
						f.set(n, fmap);
					}
				} else {
					f.set(n, parse(fieldType, value, callback));
				}
			}

			return n;
		} catch (InstantiationException e) {
			throw new RuntimeException("Primitive parse failed. Please check if nullary constructor is accessible", e);
		} catch (Exception e) {
			throw new RuntimeException("Primitive parse failed", e);
		}
	}

	public static <T> Collection<T> parseCollection(Class<T> cls, Collection<Object> coll) {
		return parseCollection(cls, coll, null);
	}

	@SuppressWarnings("unchecked")
	public static <T> Collection<T> parseCollection(Class<T> cls, Collection<Object> coll, PrimitiveParseCallback callback) {
		Collection<T> result = new ArrayList<T>();
		for (Object obj : coll) {
			if (Map.class.isAssignableFrom(obj.getClass()))
				result.add(parse(cls, obj, callback));
			else
				result.add((T) obj);
		}
		return result;
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