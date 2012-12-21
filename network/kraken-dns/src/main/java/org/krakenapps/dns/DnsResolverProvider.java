package org.krakenapps.dns;

public interface DnsResolverProvider {
	String getName();

	DnsResolver newResolver();
}
