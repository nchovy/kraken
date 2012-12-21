package org.krakenapps.dns;

import java.io.IOException;
import java.util.List;

public interface DnsService {
	DnsServiceStatus getStatus();

	void open() throws IOException;

	void close();

	void reload(DnsServiceConfig config);

	DnsCache getCache();

	List<DnsResolverProvider> getResolverProviders();

	void registerProvider(DnsResolverProvider provider);

	void unregisterProvider(DnsResolverProvider provider);

	DnsResolver newDefaultResolver();

	void setDefaultResolverProvider(String name);

	void addListener(DnsEventListener listener);

	void removeListener(DnsEventListener listener);
}
