package org.krakenapps.logstorage;

import java.util.List;
import java.util.Map;

public interface LogQuery extends Runnable {
	int getId();

	String getQueryString();

	boolean isEnd();

	void cancel();

	List<Map<String, Object>> getResult();
}
