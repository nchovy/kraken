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
 package org.krakenapps.arpwatch.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.krakenapps.arpwatch.ArpCache;
import org.krakenapps.arpwatch.ArpCacheListener;
import org.krakenapps.arpwatch.ArpEntry;
import org.krakenapps.arpwatch.ArpSpoofDetector;
import org.krakenapps.arpwatch.ArpSpoofEvent;
import org.krakenapps.arpwatch.ArpSpoofEventListener;
import org.krakenapps.filter.DefaultFilter;
import org.krakenapps.filter.DefaultMessageSpec;
import org.krakenapps.filter.FilterChain;
import org.krakenapps.filter.MessageBuilder;
import org.krakenapps.filter.MessageSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArpEventSource extends DefaultFilter implements ArpCacheListener, ArpSpoofEventListener {
	private final Logger logger = LoggerFactory.getLogger(ArpEventSource.class.getName());
	private FilterChain chain;
	private final MessageSpec spec;
	private ArpCache cache;
	private ArpSpoofDetector detector;

	public ArpEventSource() {
		spec = new DefaultMessageSpec("kraken.syslog.sender", 1, 1);
	}

	public void validate() {
		logger.info("kraken-arpwatch: registering arp event source");
		cache.register(this);
		detector.register(this);
	}

	public void invalidate() {
		if (cache != null)
			cache.unregister(this);

		if (detector != null)
			detector.unregister(this);

		logger.info("kraken-arpwatch: arp event source unregistered");
	}

	@Override
	public MessageSpec getOutputMessageSpec() {
		return spec;
	}

	@Override
	public void underAttack(ArpSpoofEvent event) {
		logger.info("kraken-arpwatch: generate attack log");

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

		String message = String.format("kraken-arpwatch: date=%s, type=arp_spoof, mac=%s, ip=%s", dateFormat
				.format(event.getDate()), event.getAttackerMac(), event.getSpoofedIp().getHostAddress());

		MessageBuilder mb = new MessageBuilder(spec);
		mb.set("severity", 1); // alert
		mb.set("message", message);
		chain.process(mb.build());
	}

	@Override
	public void entryAdded(ArpEntry entry) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		String message = String.format("kraken-arpwatch: date=%s, type=new_entry, mac=%s, ip=%s", dateFormat
				.format(new Date()), entry.getMacAddress(), entry.getIpAddress().getHostAddress());

		MessageBuilder mb = new MessageBuilder(spec);
		mb.set("severity", 6); // informational
		mb.set("message", message);
		chain.process(mb.build());
	}

	@Override
	public void entryChanged(ArpEntry oldEntry, ArpEntry newEntry) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		String message = String.format(
				"kraken-arpwatch: date=%s, type=change_entry, old_mac=%s, old_ip=%s, new_mac=%s, new_ip=%s", dateFormat
						.format(new Date()), oldEntry.getMacAddress(), oldEntry.getIpAddress().getHostAddress(),
				newEntry.getMacAddress(), newEntry.getIpAddress().getHostAddress());

		MessageBuilder mb = new MessageBuilder(spec);
		mb.set("severity", 5); // notice
		mb.set("message", message);
		chain.process(mb.build());
	}

	@Override
	public void entryRemoved(ArpEntry entry) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		String message = String.format("kraken-arpwatch: date=%s, type=remove_entry, mac=%s, ip=%s", dateFormat
				.format(new Date()), entry.getMacAddress(), entry.getIpAddress().getHostAddress());

		MessageBuilder mb = new MessageBuilder(spec);
		mb.set("severity", 6); // informational
		mb.set("message", message);
		chain.process(mb.build());
	}

	@Override
	public void entryUpdated(ArpEntry entry) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		String message = String.format("kraken-arpwatch: date=%s, type=extend_entry, mac=%s, ip=%s, first_seen=%s",
				dateFormat.format(new Date()), entry.getMacAddress(), entry.getIpAddress().getHostAddress(), dateFormat
						.format(entry.getFirstSeen()));

		MessageBuilder mb = new MessageBuilder(spec);
		mb.set("severity", 6); // informational
		mb.set("message", message);
		chain.process(mb.build());
	}

}
