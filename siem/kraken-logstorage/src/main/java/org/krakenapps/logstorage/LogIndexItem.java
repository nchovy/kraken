package org.krakenapps.logstorage;

import java.util.Date;

public interface LogIndexItem {
	String getTableName();
	
	Date getDay();

	long getLogId();
}
