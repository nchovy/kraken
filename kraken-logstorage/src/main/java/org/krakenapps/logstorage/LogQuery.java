package org.krakenapps.logstorage;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LogQuery extends Runnable {
	int getId();

	String getQueryString();

	boolean isEnd();

	void cancel();

	List<Map<String, Object>> getResult();

	List<Map<String, Object>> getResult(int offset, int limit);

	List<LogQueryCommand> getCommands();

	void registerQueryCallback(LogQueryCallback callback);

	void unregisterQueryCallback(LogQueryCallback callback);

	Set<LogTimelineCallback> getTimelineCallbacks();

	void registerTimelineCallback(LogTimelineCallback callback);

	void unregisterTimelineCallback(LogTimelineCallback callback);
}
