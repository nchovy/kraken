package org.krakenapps.logdb.pcap.impl;

import java.util.Arrays;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.logdb.SyntaxProvider;
import org.krakenapps.logdb.pcap.PcapQueryService;

@Component(name = "logdb-pcap-query")
@Provides
public class PcapQueryServiceImpl implements PcapQueryService {
	@Requires
	private SyntaxProvider syntaxProvider;

	private PcapCommandParser parser;

	@Validate
	public void start() {
		this.parser = new PcapCommandParser();
		syntaxProvider.addParsers(Arrays.asList(parser));
	}

	@Invalidate
	public void stop() {
		if (syntaxProvider != null)
			syntaxProvider.removeParsers(Arrays.asList(parser));
	}
}
