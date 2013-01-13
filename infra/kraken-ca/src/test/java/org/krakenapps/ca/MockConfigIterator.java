/*
 * Copyright 2013 Future Systems, Inc.
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
package org.krakenapps.ca;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.krakenapps.api.PrimitiveParseCallback;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigParser;
import org.krakenapps.confdb.ObjectBuilder;

public class MockConfigIterator implements ConfigIterator {

	private Iterator<Config> it;
	private Collection<Config> configs;

	public MockConfigIterator(Collection<?> docs) {
		configs = new ArrayList<Config>();
		for (Object o : docs) {
			configs.add(new MockConfig(o));
		}
		it = configs.iterator();
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public Config next() {
		return it.next();
	}

	@Override
	public void remove() {
	}

	@Override
	public void setParser(ConfigParser parser) {
	}

	@Override
	public List<Config> getConfigs(int offset, int limit) {
		return null;
	}

	@Override
	public Collection<Object> getDocuments() {
		return null;
	}

	@Override
	public <T> Collection<T> getDocuments(Class<T> cls) {
		return null;
	}

	@Override
	public <T> Collection<T> getDocuments(Class<T> cls, PrimitiveParseCallback callback) {
		return null;
	}

	@Override
	public <T> Collection<T> getDocuments(Class<T> cls, PrimitiveParseCallback callback, int offset, int limit) {
		return null;
	}

	@Override
	public <T> Collection<T> getObjects(ObjectBuilder<T> builder, int offset, int limit) {
		return null;
	}

	@Override
	public int count() {
		return 0;
	}

	@Override
	public void close() {
	}

	private class MockConfig implements Config {
		private Object doc;

		public MockConfig(Object doc) {
			this.doc = doc;
		}

		@Override
		public ConfigDatabase getDatabase() {
			return null;
		}

		@Override
		public ConfigCollection getCollection() {
			return null;
		}

		@Override
		public int getId() {
			return 0;
		}

		@Override
		public long getRevision() {
			return 0;
		}

		@Override
		public long getPrevRevision() {
			return 0;
		}

		@Override
		public Object getDocument() {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getDocument(Class<T> cls) {
			return (T) doc;
		}

		@Override
		public <T> T getDocument(Class<T> cls, PrimitiveParseCallback callback) {
			return null;
		}

		@Override
		public void setDocument(Object doc) {
		}

		@Override
		public void update() {
		}

		@Override
		public void remove() {
		}

		@Override
		public Config duplicate() {
			return null;
		}

	}

}
