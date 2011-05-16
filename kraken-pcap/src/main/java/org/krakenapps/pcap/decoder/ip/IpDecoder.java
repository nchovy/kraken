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
package org.krakenapps.pcap.decoder.ip;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ethernet.EthernetProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindori
 */
public class IpDecoder implements EthernetProcessor {
	private IpReassembler reassembler;
	private final Map<Integer, Set<IpProcessor>> callbacks;
	private final Logger logger = LoggerFactory.getLogger(IpDecoder.class.getName());

	public IpDecoder() {
		reassembler = new IpReassembler();
		callbacks = new HashMap<Integer, Set<IpProcessor>>();
	}

	public void register(int protocol, IpProcessor processor) {
		Set<IpProcessor> processors = callbacks.get(protocol);
		if (processors == null) {
			processors = new HashSet<IpProcessor>();
			callbacks.put(protocol, processors);
		}

		processors.add(processor);
	}

	public void unregister(int protocol, IpProcessor processor) {
		Set<IpProcessor> processors = callbacks.get(protocol);
		if (processors == null)
			return;

		processors.remove(processor);
	}

	public void process(EthernetFrame frame) {
		Ipv4Packet packet = Ipv4Packet.parse(frame.getData());
		packet.setL2Frame(frame);

		if (logger.isDebugEnabled())
			logger.debug(packet.toString());

		// (DF = 1) OR (It's Last fragment and FragmentOffset == 0)
		if ((packet.getFlags() & 0x02) == 2 || ((packet.getFlags() & 0x07) == 0 && packet.getFragmentOffset() == 0)) {
			// After, packet -> TCP
			dispatch(packet);
		} else {
			Ipv4Packet reassembled = reassembler.tryReassemble(packet);
			if (reassembled != null) {
				dispatch(reassembled);
			}
		}
		reassembler.drop();
	}

	private void dispatch(Ipv4Packet packet) {
		// dispatch packet for specific protocol
		dispatchProtocol(packet.getProtocol(), packet);

		// dispatch packet for all
		dispatchProtocol(0, packet);
	}

	private void dispatchProtocol(int protocol, Ipv4Packet packet) {
		// callback.onReceived(packet);

		Set<IpProcessor> processors = callbacks.get(protocol);
		if (processors != null)
			for (IpProcessor processor : processors)
				processor.process(packet);
		packet = null;
	}
}
