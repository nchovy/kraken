package org.krakenapps.logdb;

import java.util.Iterator;
import java.util.Map;

public interface LogResultSet extends Iterator<Map<String, Object>> {
	long size();
	
	void skip(long n);
	
	void close();
}
