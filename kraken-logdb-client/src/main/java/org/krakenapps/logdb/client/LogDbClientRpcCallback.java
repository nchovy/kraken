package org.krakenapps.logdb.client;

interface LogDbClientRpcCallback {
	void onPageLoaded(int id, int offset, int limit);

	void onEof(int id, int offset, int limit);
}
