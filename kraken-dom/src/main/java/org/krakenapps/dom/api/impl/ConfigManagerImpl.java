package org.krakenapps.dom.api.impl;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveParseCallback;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DOMException;
import org.krakenapps.dom.api.DefaultEntityEventProvider;

@Component(name = "dom-config-manager")
@Provides
public class ConfigManagerImpl implements ConfigManager {
	@Requires
	private ConfigService confsvc;

	private ConcurrentMap<String, ParseCallback> callbacks = new ConcurrentHashMap<String, ParseCallback>();

	private PrimitiveParseCallback getCallback(String domain) {
		callbacks.putIfAbsent(domain, new ParseCallback(domain));
		return callbacks.get(domain);
	}

	@Override
	public ConfigDatabase findDatabase(String domain) {
		ConfigDatabase db = confsvc.getDatabase("kraken-dom-" + domain);
		return db;
	}

	@Override
	public ConfigDatabase getDatabase(String domain) {
		ConfigDatabase db = findDatabase(domain);
		if (db == null)
			throw new DOMException("organization-not-found");
		return db;
	}

	@Override
	public ConfigCollection ensureCollection(String domain, Class<?> cls) {
		ConfigDatabase db = getDatabase(domain);
		return db.ensureCollection(cls);
	}

	@Override
	public Config findOne(ConfigDatabase db, Class<?> cls, Predicate pred, String notFoundMessage) {
		Config c = db.findOne(cls, pred);
		if (c == null)
			throw new DOMException(notFoundMessage);
		return c;
	}

	@Override
	public <T> T find(String domain, Class<T> cls, Predicate pred) {
		Config c = ensureCollection(domain, cls).findOne(pred);
		if (c == null)
			return null;
		return c.getDocument(cls, getCallback(domain));
	}

	@Override
	public <T> T get(String domain, Class<T> cls, Predicate pred, String notFoundMessage) {
		T t = find(domain, cls, pred);
		if (t == null)
			throw new DOMException(notFoundMessage);
		return t;
	}

	@Override
	public <T> void add(String domain, Class<T> cls, Predicate pred, T doc, String alreadyExistMessage,
			DefaultEntityEventProvider<T> provider) {
		ConfigDatabase db = getDatabase(domain);
		if (db.findOne(cls, pred) != null)
			throw new DOMException(alreadyExistMessage);
		db.add(doc);
		if (provider != null)
			provider.fireEntityAdded(domain, doc);
	}

	@Override
	public <T> void update(String domain, Class<T> cls, Predicate pred, T doc, String notFoundMessage,
			DefaultEntityEventProvider<T> provider) {
		ConfigDatabase db = getDatabase(domain);
		Config c = findOne(db, cls, pred, notFoundMessage);
		db.update(c, doc);
		if (provider != null)
			provider.fireEntityUpdated(domain, doc);
	}

	@Override
	public <T> void remove(String domain, Class<T> cls, Predicate pred, String notFoundMessage,
			DefaultEntityEventProvider<T> provider) {
		ConfigDatabase db = getDatabase(domain);
		Config c = findOne(db, cls, pred, notFoundMessage);
		db.remove(c);
		T doc = c.getDocument(cls, getCallback(domain));
		if (provider != null)
			provider.fireEntityRemoved(domain, doc);
	}

	@Override
	public PrimitiveParseCallback getParseCallback(String domain) {
		return callbacks.get(domain);
	}

	private class ParseCallback implements PrimitiveParseCallback {
		private String domain;
		private Map<Key, Object> cache = new WeakHashMap<Key, Object>();

		public ParseCallback(String domain) {
			this.domain = domain;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T onParse(Class<T> cls, Map<String, Object> referenceKey) {
			Key key = new Key(cls, referenceKey);
			Object value = cache.get(key);
			if (value == null) {
				value = find(domain, cls, Predicates.field(referenceKey));
				cache.put(key, value);
			}
			return (T) value;
		}

		private class Key {
			private Class<?> cls;
			private Map<String, Object> refkey;

			public Key(Class<?> cls, Map<String, Object> refkey) {
				this.cls = cls;
				this.refkey = refkey;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((cls == null) ? 0 : cls.hashCode());
				result = prime * result + ((refkey == null) ? 0 : refkey.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Key other = (Key) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (cls == null) {
					if (other.cls != null)
						return false;
				} else if (!cls.equals(other.cls))
					return false;
				if (refkey == null) {
					if (other.refkey != null)
						return false;
				} else if (!refkey.equals(other.refkey))
					return false;
				return true;
			}

			private ParseCallback getOuterType() {
				return ParseCallback.this;
			}
		}
	}
}
