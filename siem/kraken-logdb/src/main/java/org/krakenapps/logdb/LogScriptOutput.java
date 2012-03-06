package org.krakenapps.logdb;

import org.krakenapps.logdb.LogQueryCommand.LogMap;

public interface LogScriptOutput {
	void write(LogMap data);
}
