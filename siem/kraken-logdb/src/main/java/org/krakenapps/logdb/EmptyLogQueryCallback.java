package org.krakenapps.logdb;

import java.util.Map;

import org.krakenapps.logdb.query.FileBufferList;

public abstract class EmptyLogQueryCallback implements LogQueryCallback {

	@Override
	public int offset() {
		return 0;
	}

	@Override
	public int limit() {
		return 0;
	}

	@Override
	public void onQueryStatusChange() {
	}

	@Override
	public void onPageLoaded(FileBufferList<Map<String, Object>> result) {
	}

	@Override
	public void onEof() {
	}

}
