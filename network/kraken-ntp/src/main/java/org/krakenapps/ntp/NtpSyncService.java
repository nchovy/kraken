package org.krakenapps.ntp;

import java.net.InetAddress;

public interface NtpSyncService extends Runnable {
	NtpClient getNtpClient();

	void setNtpClient(NtpClient client);

	InetAddress getTimeServer();

	void setTimeServer(InetAddress timeServer);

	int getTimeout();

	void setTimeout(int timeout);

	String getSchedule();

	void setSchedule(String exp);

	void start();

	void stop();

	boolean isRunning();
	
	void addListener(NtpSyncListener listener);
	
	void removeListener(NtpSyncListener listener);
}
