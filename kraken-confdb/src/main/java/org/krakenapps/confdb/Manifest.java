package org.krakenapps.confdb;

import java.util.Set;

public interface Manifest {
	int getId();

	void setId(int id);

	Set<String> getCollectionNames();

	int getCollectionId(String name);

	CollectionEntry getCollectionEntry(String name);

	boolean containsDoc(String colName, int docId, long rev);

	void add(CollectionEntry e);

	void remove(CollectionEntry e);

	void add(ConfigEntry e);

	void remove(ConfigEntry e);

	byte[] serialize();
}
