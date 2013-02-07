/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
