package org.krakenapps.dom.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveParseCallback;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DOMException;
import org.krakenapps.dom.api.DefaultEntityEventProvider;

@Component(name = "dom-config-manager")
@Provides
public class ConfigManagerImpl implements ConfigManager {
	private static final String COMMITER = "kraken-dom";
	private static final boolean IGNORE_CONFLICT = true;

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
	public <T> Collection<T> all(String domain, Class<T> cls) {
		return all(domain, cls, null);
	}

	@Override
	public <T> Collection<T> all(String domain, Class<T> cls, Predicate pred) {
		return getDatabase(domain).ensureCollection(cls).find(pred).getDocuments(cls, getCallback(domain));
	}

	@Override
	public <T> T find(String domain, Class<T> cls, Predicate pred) {
		Config c = getDatabase(domain).ensureCollection(cls).findOne(pred);
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
	public <T> void adds(String domain, Class<T> cls, List<Predicate> preds, List<T> docs, String alreadyExistMessage,
			DefaultEntityEventProvider<T> provider) {
		if (preds.size() != docs.size())
			throw new IllegalArgumentException("preds and docs must has equal size");

		ConfigDatabase db = getDatabase(domain);
		ConfigTransaction xact = db.beginTransaction();

		Iterator<Predicate> predIterator = preds.iterator();
		Iterator<T> docIterator = docs.iterator();
		try {
			while (docIterator.hasNext()) {
				Predicate pred = predIterator.next();
				T doc = docIterator.next();

				if (db.findOne(cls, pred) != null)
					throw new DOMException(alreadyExistMessage);

				db.add(xact, doc);
			}

			xact.commit(COMMITER, "added " + docs.size() + " " + cls.getSimpleName());
		} catch (Exception e) {
			xact.rollback();
			if (e instanceof DOMException)
				throw (DOMException) e;
			throw new RuntimeException(e);
		}

		if (provider != null) {
			for (T doc : docs)
				provider.fireEntityAdded(domain, doc);
		}
	}

	@Override
	public <T> void add(String domain, Class<T> cls, Predicate pred, T doc, String alreadyExistMessage,
			DefaultEntityEventProvider<T> provider) {
		ConfigDatabase db = getDatabase(domain);
		if (db.findOne(cls, pred) != null)
			throw new DOMException(alreadyExistMessage);
		db.add(doc, COMMITER, "added 1 " + cls.getSimpleName());
		if (provider != null)
			provider.fireEntityAdded(domain, doc);
	}

	@Override
	public <T> void updates(String domain, Class<T> cls, List<Predicate> preds, List<T> docs, String notFoundMessage,
			DefaultEntityEventProvider<T> provider) {
		if (preds.size() != docs.size())
			throw new IllegalArgumentException("preds and docs must has equal size");

		ConfigDatabase db = getDatabase(domain);
		ConfigTransaction xact = db.beginTransaction();

		Iterator<Predicate> predIterator = preds.iterator();
		Iterator<T> docIterator = docs.iterator();
		try {
			while (docIterator.hasNext()) {
				Predicate pred = predIterator.next();
				T doc = docIterator.next();

				Config c = db.findOne(cls, pred);
				if (c == null)
					throw new DOMException(notFoundMessage);

				db.update(xact, c, doc, IGNORE_CONFLICT);
			}

			xact.commit(COMMITER, "updated " + docs.size() + " " + cls.getSimpleName());
		} catch (Exception e) {
			xact.rollback();
			if (e instanceof DOMException)
				throw (DOMException) e;
			throw new RuntimeException(e);
		}

		if (provider != null) {
			for (T doc : docs)
				provider.fireEntityUpdated(domain, doc);
		}
	}

	@Override
	public <T> void update(String domain, Class<T> cls, Predicate pred, T doc, String notFoundMessage,
			DefaultEntityEventProvider<T> provider) {
		ConfigDatabase db = getDatabase(domain);
		Config c = get(db, cls, pred, notFoundMessage);
		db.update(c, doc, IGNORE_CONFLICT, COMMITER, "updated 1 " + cls.getSimpleName());
		if (provider != null)
			provider.fireEntityUpdated(domain, doc);
	}

	@Override
	public <T> void removes(String domain, Class<T> cls, List<Predicate> preds, String notFoundMessage,
			DefaultEntityEventProvider<T> provider) {
		ConfigDatabase db = getDatabase(domain);
		ConfigTransaction xact = db.beginTransaction();

		Iterator<Predicate> predIterator = preds.iterator();
		Collection<T> docs = new ArrayList<T>();
		try {
			while (predIterator.hasNext()) {
				Predicate pred = predIterator.next();

				Config c = db.findOne(cls, pred);
				if (c == null)
					throw new DOMException(notFoundMessage);

				T doc = c.getDocument(cls);
				if (provider != null)
					provider.fireEntityRemoving(domain, doc);
				docs.add(doc);

				db.remove(xact, c, IGNORE_CONFLICT);
			}

			xact.commit(COMMITER, "removed " + preds.size() + " " + cls.getSimpleName());
		} catch (Exception e) {
			xact.rollback();
			if (e instanceof DOMException)
				throw (DOMException) e;
			throw new RuntimeException(e);
		}

		if (provider != null) {
			for (T doc : docs)
				provider.fireEntityRemoved(domain, doc);
		}
	}

	@Override
	public <T> void remove(String domain, Class<T> cls, Predicate pred, String notFoundMessage, DefaultEntityEventProvider<T> provider) {
		ConfigDatabase db = getDatabase(domain);
		Config c = get(db, cls, pred, notFoundMessage);
		T doc = c.getDocument(cls, getCallback(domain));
		if (provider != null)
			provider.fireEntityRemoving(domain, doc);
		db.remove(c, IGNORE_CONFLICT, COMMITER, "removed 1 " + cls.getSimpleName());
		if (provider != null)
			provider.fireEntityRemoved(domain, doc);
	}

	private Config get(ConfigDatabase db, Class<?> cls, Predicate pred, String notFoundMessage) {
		Config c = db.findOne(cls, pred);
		if (c == null)
			throw new DOMException(notFoundMessage);
		return c;
	}

	@Override
	public PrimitiveParseCallback getParseCallback(String domain) {
		return getCallback(domain);
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
