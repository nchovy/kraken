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

package org.krakenapps.pcap.decoder.ethernet;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.krakenapps.pcap.packet.PcapPacket;
import org.krakenapps.pcap.util.Buffer;

/**
 * @author mindori
 */
public class EthernetDecoder {
	private Set<EthernetProcessor> callbacks;
	private final Map<Integer, Set<EthernetProcessor>> typeCallbacks;
	private static final int IEEE_8021AQ = 0x8100;
	private static final int IEEE_8021AD = 0x9100;
	
	public EthernetDecoder() {
		callbacks = new CopyOnWriteArraySet<EthernetProcessor>();
		typeCallbacks = new ConcurrentHashMap<Integer, Set<EthernetProcessor>>();
	}

	public void register(EthernetProcessor processor) {
		this.callbacks.add(processor);
	}

	public void register(int type, EthernetProcessor processor) {
		Set<EthernetProcessor> processors = typeCallbacks.get(type);
		if (processors == null) {
			processors = new HashSet<EthernetProcessor>();
			typeCallbacks.put(type, processors);
		}

		processors.add(processor);
	}

	public void unregister(EthernetProcessor processor) {
		this.callbacks.remove(processor);
	}

	public void unregister(int type, EthernetProcessor processor) {
		Set<EthernetProcessor> processors = typeCallbacks.get(type);
		if (processors == null)
			return;

		processors.remove(processor);
	}

	public void decode(PcapPacket packet) {
		// do not reorder following codes (parse sequence)
		MacAddress destination = getMacAddress(packet.getPacketData());
		MacAddress source = getMacAddress(packet.getPacketData());
		int type = getEtherType(packet.getPacketData());
		
		if (type == IEEE_8021AQ) {
		    IEEE_802_1Q iee802_1aqTag = get802_1qTag(packet.getPacketData());
		    type = getEtherType(packet.getPacketData());
		    if (type == IEEE_8021AD) {
			IEEE_802_1Q iee802_1adTag = get802_1qTag(packet.getPacketData());
			type = getEtherType(packet.getPacketData());		    
		    }
		}
		
		Buffer buffer = packet.getPacketData();
		buffer.discardReadBytes();

		EthernetFrame frame = new EthernetFrame(source, destination, type, buffer);
		frame.setPcapPacket(packet);
		dispatch(frame);
	}

	private MacAddress getMacAddress(Buffer data) {
		byte[] mac = new byte[6];
		data.gets(mac, 0, 6);
		return new MacAddress(mac);
	}

	private int getEtherType(Buffer data) {
		return ((int) data.getShort()) & 0x0000FFFF;
	}

	private void dispatch(EthernetFrame frame) {
		for (EthernetProcessor processor : callbacks)
			processor.process(frame);

		Set<EthernetProcessor> processors = typeCallbacks.get(frame.getType());
		if (processors == null)
			return;

		for (EthernetProcessor processor : processors)
			processor.process(frame.dup());
	}
	
	/**
	 * @see http://en.wikipedia.org/wiki/IEEE_802.1Q
	 * @param data
	 * @return
	 */
	private IEEE_802_1Q get802_1qTag(Buffer data) {
	    byte[] tagField = new byte[2];
	    data.gets(tagField, 0, 2);
	    BitSet bits = BitSet.valueOf(tagField);
	    int pcp = convertBitToInt(bits.get(0, 3));
	    int dei = convertBitToInt(bits.get(3, 4));
	    int vid = convertBitToInt(bits.get(4, 16));
	    return new IEEE_802_1Q(pcp, dei, vid);
	}

	private int convertBitToInt(BitSet bits) {
	    int value = 0;
	    for (int i = 0; i < bits.length(); ++i) {
		value += bits.get(i) ? (1 << i) : 0;
	    }
	    return value;
	}
}