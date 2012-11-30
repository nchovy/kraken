package org.krakenapps.ntp;

import java.util.Date;

public interface NtpSyncListener {
	void onSetTime(Date newTime);
}
