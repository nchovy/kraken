package org.krakenapps.logger;

import org.krakenapps.api.EnumAutoCompleter;

public class LogLevelAutoCompleter extends EnumAutoCompleter {
	public LogLevelAutoCompleter() {
		super("debug", "trace", "info", "warn", "error");
	}
}
