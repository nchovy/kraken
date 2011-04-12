package org.krakenapps.sleepproxy;

public interface LogProvider {
	void register(LogListener callback);

	void unregister(LogListener callback);
}
