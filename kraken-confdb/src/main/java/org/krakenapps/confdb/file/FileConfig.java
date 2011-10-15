package org.krakenapps.confdb.file;

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
	public void setDocument(Object doc) {
		this.doc = doc;
	}

	@Override
	public String toString() {
		return "id=" + id + ", rev=" + rev + ", prev=" + prevRev + ", doc=" + doc;
	}
}
