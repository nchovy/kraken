package org.krakenapps.logstorage;

import java.util.Map;

import org.krakenapps.logstorage.query.FileBufferList;

public interface LogQueryCallback {
	void callback(FileBufferList<Map<String, Object>> result);
}
