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
package org.krakenapps.arpwatch;

import java.net.InetAddress;
import java.util.Collection;

import org.krakenapps.pcap.decoder.arp.ArpPacket;

public interface ArpCache {
	Collection<ArpEntry> getCachedEntries();
	
	ArpEntry find(InetAddress ip);

	void add(ArpPacket p);

	void flush();

	void register(ArpCacheListener listener);

	void unregister(ArpCacheListener listener);
}
