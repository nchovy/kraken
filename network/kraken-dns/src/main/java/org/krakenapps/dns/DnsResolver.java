package org.krakenapps.dns;

import java.io.IOException;

public interface DnsResolver {
	DnsMessage resolve(DnsMessage query) throws IOException;
}
