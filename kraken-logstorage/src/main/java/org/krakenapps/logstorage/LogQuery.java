package org.krakenapps.logstorage;

import java.util.List;
import java.util.Map;

public interface LogQuery extends Runnable {
	int getId();

	String getQueryString();

	boolean isEnd();

	void cancel();

	List<Map<String, Object>> getResult();

	List<Map<String, Object>> getResult(int offset, int limit);

	List<LogQueryCommand> getCommands();

	void registerCallback(LogQueryCallback callback);

	void unregisterCallback(LogQueryCallback callback);
}
