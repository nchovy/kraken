package org.krakenapps.portmon;

import java.util.Date;

public interface PortStatus {
	Date getLastCheckTime();

	boolean getLastStatus();
}
