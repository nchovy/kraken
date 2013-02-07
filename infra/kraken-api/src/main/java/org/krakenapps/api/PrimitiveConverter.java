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
package org.krakenapps.api;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

	public static enum SerializeOption {
		INCLUDE_SKIP_FIELD
	}

	public static Object serialize(Object obj) {
		return serialize(obj, obj, null, null);
	}

	public static Object serialize(Object obj, PrimitiveSerializeCallback callback) {
		return serialize(obj, obj, callback, null);
	}

	public static Object serialize(Object obj, SerializeOption... options) {
		return serialize(obj, obj, null, null, options);
	}

	public static Object serialize(Object obj, PrimitiveSerializeCallback callback, SerializeOption... options) {
		return serialize(obj, obj, callback, null, options);
	}

	@SuppressWarnings("unchecked")
	private static Object serialize(Object root, Object obj, PrimitiveSerializeCallback callback, List<String> refkey,
			SerializeOption... options) {
		if (obj == null)
			return null;

		List<SerializeOption> optionList = Arrays.asList(options);

		Class<?> cls = obj.getClass();

		// Primitive Object
		if (nonSerializeClasses.contains(cls))
			return obj;

		// Array
		if (cls.isArray()) {
			if (cls.getComponentType().isPrimitive())
				return obj;
			Object[] ary = new Object[Array.getLength(obj)];
			for (int i = 0; i < ary.length; i++)
				ary[i] = serialize(root, Array.get(obj, i), callback, refkey, options);
			return ary;
		}

		// Collection
		if (Collection.class.isAssignableFrom(cls)) {
			Collection<Object> col = new ArrayList<Object>();
			for (Object o : (Collection<Object>) obj)
				col.add(serialize(root, o, callback, refkey, options));
			return col;
		}

		// Map
		if (Map.class.isAssignableFrom(cls)) {
			Map<Object, Object> m = new HashMap<Object, Object>();
			Map<Object, Object> o = (Map<Object, Object>) obj;
			for (Object key : o.keySet()) {
				Object k = serialize(root, key, callback, refkey, options);
				Object value = o.get(key);
				Object v = serialize(root, value, callback, refkey, options);
				m.put(k, v);
			}
			return m;
		}

		// Others
		Map<String, Object> m = new HashMap<String, Object>();
		for (Field f : cls.getDeclaredFields()) {
			// skip static field
			if (Modifier.isStatic(f.getModifiers()))
				continue;

			try {
				FieldOption option = f.getAnnotation(FieldOption.class);

				if (option != null && option.skip() && !optionList.contains(SerializeOption.INCLUDE_SKIP_FIELD))
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
					if (option != null && option.length() > 0 && ((String) value).length() > option.length()) {
						String s = (String) value;
						throw new IllegalArgumentException(String.format(
								"Too long String value for %s.%s (limit: %d, input: [%s])", cls.getName(), f.getName(),
								option.length(), s));
					}
					m.put(fieldName, value);
				} else {
					if (value == null)
						m.put(fieldName, null);
					else {
						List<String> referenceKey = null;
						if (f.getAnnotation(ReferenceKey.class) != null && callback != null)
							referenceKey = Arrays.asList(f.getAnnotation(ReferenceKey.class).value());
						Object serialized = serialize(root, value, callback, referenceKey, options);
						m.put(fieldName, serialized);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("kraken api: serialize failed", e);
			}
		}
		if (callback != null && refkey != null && !(refkey.containsAll(m.keySet()) && m.keySet().containsAll(refkey)))
			callback.onSerialize(root, cls, obj, m);

		return m;
	}

	public static <T> T parse(Class<T> cls, Object obj) {
		return parse(cls, obj, null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T parse(Class<T> cls, Object obj, PrimitiveParseCallback callback) {
		if (obj == null || !(obj instanceof Map) || cls.equals(Object.class))
			return (T) obj;

		try {
			Map<String, Object> m = (Map<String, Object>) obj;

			T n = null;
			try {
				n = cls.newInstance();
			} catch (IllegalAccessException e) {
				Constructor c = cls.getDeclaredConstructor();
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

				if (!m.containsKey(fieldName))
					continue;

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
							f.set(n, callback.onParse(fieldType, getRefKeys(keys, refkey.value())));
						else if (option != null && !option.nullable())
							throw new IllegalArgumentException(fieldName + " requires parse callback");
					} else if (value instanceof Object[]) {
						List<Object> coll = new ArrayList<Object>();
						for (Object v : (Object[]) value) {
							Map<String, Object> keys = (Map<String, Object>) v;
							if (callback != null) {
								Object o = callback.onParse(f.getAnnotation(CollectionTypeHint.class).value(),
										getRefKeys(keys, refkey.value()));
								if (o != null)
									coll.add(o);
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
					}
					continue;
				}

				if (fieldType.isEnum()) {
					Object found = null;
					for (Object o : fieldType.getEnumConstants())
						if (o.toString().equals(value))
							found = o;
					f.set(n, found);
				} else if (fieldType.isArray()) {
					if (fieldType.getComponentType().isPrimitive())
						f.set(n, value);
					else {
						Object[] o = (Object[]) value;
						Object ary = Array.newInstance(fieldType.getComponentType(), o.length);
						for (int i = 0; i < o.length; i++)
							Array.set(ary, i, o[i]);
						f.set(n, ary);
					}
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

	private static Map<String, Object> getRefKeys(Map<String, Object> src, String[] refkeys) {
		Map<String, Object> m = new HashMap<String, Object>();
		for (String key : refkeys) {
			key = toUnderscoreName(key);
			m.put(key, src.get(key));
		}
		return m;
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

	public static Object overwrite(Object obj, Map<String, Object> m) {
		return overwrite(obj, m, (PrimitiveParseCallback) null);
	}

	public static Object overwrite(Object obj, Map<String, Object> m, PrimitiveParseCallback callback) {
		Object newObj = parse(obj.getClass(), m, callback);
		return overwrite(obj, newObj, m);
	}

	@SuppressWarnings("unchecked")
	private static Object overwrite(Object before, Object after, Map<String, Object> m) {
		if (before == null || after == null)
			return after;

		try {
			for (Field f : before.getClass().getDeclaredFields()) {
				String fieldName = toUnderscoreName(f.getName());
				if (m.containsKey(fieldName)) {
					f.setAccessible(true);

					Class<?> cls = f.getType();
					Object newValue = m.get(fieldName);
					if (newValue != null && Map.class.isAssignableFrom(newValue.getClass()) && !Map.class.isAssignableFrom(cls))
						f.set(before, overwrite(f.get(before), f.get(after), (Map<String, Object>) newValue));
					else
						f.set(before, f.get(after));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Primitive overwrite failed", e);
		}
		return before;
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