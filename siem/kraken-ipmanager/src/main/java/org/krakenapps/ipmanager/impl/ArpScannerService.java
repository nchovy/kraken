/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.ipmanager.impl;

import java.net.InetAddress;
import java.util.Date;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.cron.PeriodicJob;
import org.krakenapps.ipmanager.ArpScanner;
import org.krakenapps.ipmanager.IpDetection;
import org.krakenapps.ipmanager.IpManager;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.live.PcapDeviceManager;
import org.krakenapps.pcap.live.PcapDeviceMetadata;
import org.krakenapps.pcap.util.Arping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "ipm-arp-scanner")
@Provides
@PeriodicJob("*/5 * * * *")
public class ArpScannerService implements ArpScanner {
	private final Logger logger = LoggerFactory.getLogger(ArpScannerService.class.getName());

	@Requires
	private IpManager ipManager;

	private int timeout = 10000; // 10sec by default

	@Override
	public int getTimeout() {
		return timeout;
	}

	@Override
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public void run() {
		if (ipManager == null)
			return;

		logger.trace("kraken ipmanager: begin arp scanning");

		for (PcapDeviceMetadata metadata : PcapDeviceManager.getDeviceMetadataList()) {
			try {
				logger.trace("kraken ipmanager: begin arp scanning for [{}]", metadata.getName());

				Date now = new Date();
				Map<InetAddress, MacAddress> m = Arping.scan(metadata.getName(), timeout);

				for (InetAddress ip : m.keySet()) {
					MacAddress mac = m.get(ip);
					IpDetection d = new IpDetection("local", now, mac, ip);
					if (ipManager != null)
						ipManager.updateIpEntry(d);

					logger.trace("kraken ipmanager: found arp entry [{}, {}]", ip, mac);
				}
			} catch (Exception e) {
				logger.error("kraken ipmanager: arp scan failed", e);
			}
		}

		logger.trace("kraken ipmanager: arp scanning ended");
	}
}
