package org.krakenapps.logstorage;

import java.util.Iterator;

public interface LogIndexCursor extends Iterator<LogIndexItem> {
	void skip(long offset);
	
	void close();
}
