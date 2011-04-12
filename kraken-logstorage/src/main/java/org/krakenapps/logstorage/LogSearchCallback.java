package org.krakenapps.logstorage;

public interface LogSearchCallback extends LogCallback {
	void interrupt();

	boolean isInterrupted();
}
