package org.krakenapps.logdb;

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
	public void onPageLoaded() {
	}

	@Override
	public void onEof() {
	}

}
