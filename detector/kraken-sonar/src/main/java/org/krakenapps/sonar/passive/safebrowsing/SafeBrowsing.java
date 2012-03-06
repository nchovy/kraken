package org.krakenapps.sonar.passive.safebrowsing;

public interface SafeBrowsing {
	void start();

	void stop();

	void updateMalware();

	void updateBlacklist();

	int SafeCheck(String url);
}
