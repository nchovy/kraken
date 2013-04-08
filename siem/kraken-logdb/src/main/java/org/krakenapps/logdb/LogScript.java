package org.krakenapps.logdb;

import java.util.Map;

public interface LogScript {
	void init(Map<String, Object> params);
	
	void handle(LogScriptInput input, LogScriptOutput output);
	
	void eof(LogScriptOutput output);
}
