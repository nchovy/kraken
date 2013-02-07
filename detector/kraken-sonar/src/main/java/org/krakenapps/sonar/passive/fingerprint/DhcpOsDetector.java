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
import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.decoder.dhcp.DhcpMessage;
import org.krakenapps.pcap.decoder.dhcp.DhcpProcessor;
import org.krakenapps.pcap.decoder.dhcp.fingerprint.FingerprintDetector;
import org.krakenapps.pcap.decoder.dhcp.fingerprint.FingerprintMetadata;
import org.krakenapps.pcap.decoder.dhcp.options.DhcpOption;
import org.krakenapps.pcap.decoder.dhcp.options.ParameterRequestListOption;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.sonar.Metabase;
import org.krakenapps.sonar.PassiveScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "sonar-dhcp-os-detector")
@Provides
public class DhcpOsDetector implements DhcpProcessor {
	private final Logger logger = LoggerFactory.getLogger(DhcpOsDetector.class.getName());

	@Requires
	private PassiveScanner scanner;

	@Requires
	private Metabase metabase;

	@Validate
	public void start() {
		scanner.addUdpSniffer(Protocol.DHCP, this);

		logger.info("kraken sonar: dhcp os detector started");
	}

	@Invalidate
	public void stop() {
		if (scanner != null)
			scanner.removeUdpSniffer(Protocol.DHCP, this);

		logger.info("kraken sonar: dhcp os detector stopped");
	}

	@Override
	public void process(DhcpMessage msg) {
		StringBuilder sb = new StringBuilder();
		String finger = null;

		MacAddress macAddress = null;
		FingerprintMetadata fm = null;

		int i = 0;
		for (DhcpOption option : msg.getOptions()) {
			if (i != 0)
				sb.append(",");

			sb.append(option.getType());
			if (option instanceof ParameterRequestListOption) {
				ParameterRequestListOption o = (ParameterRequestListOption) option;
				fm = FingerprintDetector.matches(o.getFingerprint());
				if (fm != null) {
					macAddress = msg.getClientMac();
					finger = "client ip: " + msg.getClientAddress() + " client mac: " + msg.getClientMac()
							+ " your ip: " + msg.getYourAddress() + " finger: " + o.getFingerprint()
							+ " metadata: " + fm.toString();
				}
			}
			i++;
		}

		if (macAddress != null && fm != null) {
			metabase.updateIpEndpoint(macAddress,
					metabase.updateEnvironment(
					metabase.updateVendor(fm.getVendor()),
					fm.getFamily(),
					fm.getDescription()));
		}

		String options = sb.toString();
		logger.trace("kraken sonar: dhcp options [{}], finger [{}]", options, finger);
	}

}