package org.krakenapps.logstorage;

import java.util.Iterator;

public interface LogCursor extends Iterator<Log> {
	void close();
}
