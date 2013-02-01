package org.krakenapps.logdb;

import java.util.Map;

public class BaseLogScript implements LogQueryScript {

	@Override
	public void init(Map<String, Object> params) {
	}

	@Override
	public void handle(LogQueryScriptInput input, LogQueryScriptOutput output) {
	}

	@Override
	public void eof(LogQueryScriptOutput output) {
	}

}
