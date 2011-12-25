package org.krakenapps.logdb;

public interface LogScript {
	void handle(LogScriptInput input, LogScriptOutput output);
	
	void eof();
}
