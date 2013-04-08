package org.krakenapps.logdb;

public interface LogQueryEventListener {
	void onQueryStatusChange(LogQuery query, LogQueryStatus status);
}
