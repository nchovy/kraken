package org.krakenapps.confdb;

public interface Config {
	ConfigDatabase getDatabase();

	ConfigCollection getCollection();

	int getId();

	long getRevision();
	
	long getPrevRevision();
	
	Object getDocument();
	
	void setDocument(Object doc);
}
