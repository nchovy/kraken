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
 package org.krakenapps.sonar;

import java.util.Collection;

import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.decoder.arp.ArpProcessor;
import org.krakenapps.pcap.util.PcapLiveRunner;

public interface PassiveScanner {
	void start();

	void stop();

	Collection<String> getDeviceNames();

	PcapLiveRunner getDevice(String name);

	void addArpSniffer(ArpProcessor callback);

	void removeArpSniffer(ArpProcessor callback);

	void addTcpSniffer(Protocol protocol, Object callback);

	void removeTcpSniffer(Protocol protocol, Object callback);

	void addUdpSniffer(Protocol protocol, Object callback);

	void removeUdpSniffer(Protocol protocol, Object callback);
}
