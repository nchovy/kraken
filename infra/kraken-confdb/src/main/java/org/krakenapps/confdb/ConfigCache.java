package org.krakenapps.confdb;

public interface ConfigCache {
	Config findEntry(String colName, int docId, long rev);

	void putEntry(String colName, Config c);
}
