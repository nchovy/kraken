package org.krakenapps.logdb;

public interface LogQueryEventListener {
	void onCreate(LogQuery query);

	void onRemove(LogQuery query);

	void onStart(LogQuery query);

	void onStop(LogQuery query);

	void onEof(LogQuery query);

}
