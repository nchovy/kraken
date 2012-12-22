package org.krakenapps.dns;

import java.io.IOException;

public interface DnsService {
	DnsServiceStatus getStatus();

	void open() throws IOException;

	void close();

	void reload(DnsServiceConfig config);

	DnsCache getCache();

	void addListener(DnsEventListener listener);

	void removeListener(DnsEventListener listener);
}
