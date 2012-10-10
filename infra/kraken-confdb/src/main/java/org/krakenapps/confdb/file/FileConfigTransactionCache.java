package org.krakenapps.confdb.file;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.confdb.ConfigTransactionCache;
import org.krakenapps.confdb.ReferenceKeys;

public class FileConfigTransactionCache implements ConfigTransactionCache {
	private ConcurrentMap<Class<?>, WeakReference<Set<Object>>> cache;

	public FileConfigTransactionCache() {
		this.cache = new ConcurrentHashMap<Class<?>, WeakReference<Set<Object>>>();
	}

	@Override
	public Object get(Class<?> cls, ReferenceKeys keys) {
		Set<Object> l = getCachedObjects(cls);

		for (Object o : l)
			if (keys.eval(o))
				return o;

		return null;
	}

	@Override
	public void put(Class<?> cls, Object obj) {
		Set<Object> l = getCachedObjects(cls);
		l.add(obj);
	}

	@Override
	public void remove(Class<?> cls, Object obj) {
		Set<Object> l = getCachedObjects(cls);
		l.remove(obj);
	}

	private Set<Object> getCachedObjects(Class<?> cls) {
		WeakReference<Set<Object>> ref = cache.get(cls);
		if (ref == null) {
			WeakReference<Set<Object>> old = cache.putIfAbsent(cls, new WeakReference<Set<Object>>(new HashSet<Object>()));
			if (old != null)
				ref = old;
		}

		if (ref != null)
			return ref.get();
		return new HashSet<Object>();
	}

}
