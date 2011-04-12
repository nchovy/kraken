package org.krakenapps.safebrowsing.interfaces;

public interface SafeBrowsing {
	void start();

	void stop();

	void updateMalware();

	void updateBlacklist();

	int SafeCheck(String url);
}
