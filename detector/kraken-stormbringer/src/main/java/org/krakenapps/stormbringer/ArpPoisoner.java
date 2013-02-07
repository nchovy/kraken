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
 package org.krakenapps.stormbringer;

import java.net.InetAddress;
import java.util.Map;

import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.live.PcapDevice;

public interface ArpPoisoner extends Runnable {
	void addAdapter(PcapDevice device);

	void removeAdapter(PcapDevice device);

	void addTarget(InetAddress peer1, InetAddress peer2);

	void removeTarget(InetAddress peer1, InetAddress peer2);

	Map<InetAddress, MacAddress> getArpCache();
	
	// NOTE: will be removed
	void setGateway(InetAddress ip);

	void attack();

	void stop();
}
