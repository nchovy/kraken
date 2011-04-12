package org.krakenapps.api;

public interface LoggerControlService {
	boolean hasLogger(String name);

	void setLogLevel(String name, String level, boolean isEnabled);
}
