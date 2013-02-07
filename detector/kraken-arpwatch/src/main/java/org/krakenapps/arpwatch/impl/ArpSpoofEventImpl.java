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

import java.net.InetAddress;
import java.util.Date;

import org.krakenapps.arpwatch.ArpSpoofEvent;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;

public class ArpSpoofEventImpl implements ArpSpoofEvent {
	private final Date date;
	private final MacAddress attacker;
	private final InetAddress ip;

	public ArpSpoofEventImpl(MacAddress attacker, InetAddress ip) {
		this.date = new Date();
		this.attacker = attacker;
		this.ip = ip;
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public InetAddress getSpoofedIp() {
		return ip;
	}

	@Override
	public MacAddress getAttackerMac() {
		return attacker;
	}

}
