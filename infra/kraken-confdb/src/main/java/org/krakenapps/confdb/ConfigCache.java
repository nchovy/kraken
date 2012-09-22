package org.krakenapps.confdb;

public interface ConfigCache {
	Config findEntry(String colName, int manifsetId, int docId, long rev);

	void putEntry(String colName, int manifestId, Config c);
}
