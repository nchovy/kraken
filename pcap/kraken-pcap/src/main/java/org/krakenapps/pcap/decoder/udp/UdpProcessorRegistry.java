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
package org.krakenapps.pcap.decoder.udp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author mindori
 */
public class UdpProcessorRegistry {
	private final Map<Integer, Set<UdpProcessor>> processorMap;

	public UdpProcessorRegistry() {
		processorMap = new HashMap<Integer, Set<UdpProcessor>>();
	}

	public void register(int serverPort, UdpProcessor processor) {
		Set<UdpProcessor> processors = processorMap.get(serverPort);
		if (processors == null) {
			processors = new HashSet<UdpProcessor>();
			processorMap.put(serverPort, processors);
		}

		processors.add(processor);
	}

	public void unregister(int serverPort, UdpProcessor processor) {
		Set<UdpProcessor> processors = processorMap.get(serverPort);
		if (processors == null)
			return;

		processors.remove(processor);
	}

	public void dispatch(UdpPacket datagram, byte[] data) {
//		Set<UdpProcessor> processors = processorMap.get(datagram.getSourcePort());
//		if (processors == null) {
//			processors = processorMap.get(datagram.getDestinationPort());
//			if (processors == null) {
//				return;
//			}
//		}
//
//		for (UdpProcessor processor : processors) {
//			processor.process(datagram.getSource(), datagram.getDestination(), data);
//		}
	}
}
