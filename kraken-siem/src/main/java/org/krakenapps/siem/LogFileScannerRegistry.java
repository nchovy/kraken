package org.krakenapps.siem;

import java.util.Collection;

public interface LogFileScannerRegistry {
	Collection<LogFileScanner> getScanners();

	LogFileScanner getScanner(String name);
}
