package org.krakenapps.sonar.passive.fingerprint;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.pcap.decoder.arp.ArpPacket;
import org.krakenapps.pcap.decoder.arp.ArpProcessor;
import org.krakenapps.sonar.Metabase;
import org.krakenapps.sonar.PassiveScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "sonar-arp-detector")
@Provides
public class ArpDetector implements ArpProcessor {
	private final Logger logger = LoggerFactory.getLogger(ArpDetector.class.getName());

	@Requires
	private PassiveScanner scanner;

	@Requires
	private Metabase metabase;

	@Validate
	public void start() {
		scanner.addArpSniffer(this);
	}

	@Invalidate
	public void stop() {
		if (scanner != null)
			scanner.removeArpSniffer(this);
	}

	@Override
	public void process(ArpPacket p) {
		System.out.println("kraken sonar: " + p);

		try {
			if (!p.getSenderIp().getHostAddress().equals("0.0.0.0"))
				metabase.updateIpEndPoint(p.getSenderMac(), p.getSenderIp());
		} catch (Exception e) {
			logger.error("kraken sonar: cannot create ip endpoint: " + p.getSenderIp().getHostAddress(), e);
		}
	}

}
