package org.krakenapps.logstorage;

import java.util.Map;

import org.krakenapps.logstorage.query.FileBufferList;

public interface LogQueryCallback {
	int offset();

	int limit();

	void pageLoadedCallback(FileBufferList<Map<String, Object>> result);

	void eofCallback();
}
