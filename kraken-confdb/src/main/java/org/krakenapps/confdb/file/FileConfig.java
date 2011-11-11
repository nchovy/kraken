/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.confdb.file;

import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.api.PrimitiveParseCallback;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;

class FileConfig implements Config {
	private ConfigDatabase db;
	private ConfigCollection col;
	private int id;
	private long rev;
	private long prevRev;
	private Object doc;

	public FileConfig(ConfigDatabase db, ConfigCollection col, int id, long rev, long prevRev, Object doc) {
		this.db = db;
		this.col = col;
		this.id = id;
		this.rev = rev;
		this.prevRev = prevRev;
		this.doc = doc;
	}

	@Override
	public ConfigDatabase getDatabase() {
		return db;
	}

	@Override
	public ConfigCollection getCollection() {
		return col;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public long getRevision() {
		return rev;
	}

	@Override
	public long getPrevRevision() {
		return prevRev;
	}

	@Override
	public Object getDocument() {
		return doc;
	}

	@Override
	public <T> T getDocument(Class<T> cls) {
		return PrimitiveConverter.parse(cls, doc);
	}

	@Override
	public <T> T getDocument(Class<T> cls, PrimitiveParseCallback callback) {
		return PrimitiveConverter.parse(cls, doc, callback);
	}

	@Override
	public void setDocument(Object doc) {
		this.doc = doc;
	}

	@Override
	public String toString() {
		return "id=" + id + ", rev=" + rev + ", prev=" + prevRev + ", doc=" + doc;
	}
}
