package org.krakenapps.logstorage;

import java.util.Date;
import java.util.Map;

public interface LogTimelineCallback {
	int limit();

	void callback(int spanField, int spanAmount, Map<Date, Integer> timeline);
}
