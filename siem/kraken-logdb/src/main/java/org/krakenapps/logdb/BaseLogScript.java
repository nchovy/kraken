package org.krakenapps.logdb;

import java.util.Map;

public class BaseLogScript implements LogScript {

	@Override
	public void init(Map<String, Object> params) {
	}

	@Override
	public void handle(LogScriptInput input, LogScriptOutput output) {
	}

	@Override
	public void eof(LogScriptOutput output) {
	}

}
