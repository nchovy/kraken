package org.krakenapps.dns;

import java.util.Set;

public interface DnsCache {
	DnsCacheEntry lookup(DnsCacheKey key);

	Set<DnsCacheKey> getKeys();

	void putEntry(DnsCacheKey key, DnsCacheEntry entry);

	void removeEntry(DnsCacheKey key);

	void clear();
}
