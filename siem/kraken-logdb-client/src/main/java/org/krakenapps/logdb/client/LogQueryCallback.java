package org.krakenapps.logdb.client;

public interface LogQueryCallback {
	void onPageLoaded(int queryId);

	void onEof(int queryId);
}
