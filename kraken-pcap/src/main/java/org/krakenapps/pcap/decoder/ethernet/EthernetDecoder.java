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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.krakenapps.pcap.packet.PcapPacket;
import org.krakenapps.pcap.util.Buffer;

/**
 * @author mindori
 */
public class EthernetDecoder {
	private EthernetCallback callback;
	private final Map<Integer, Set<EthernetProcessor>> callbacks;

	public EthernetDecoder() {
		callbacks = new HashMap<Integer, Set<EthernetProcessor>>();
	}
	
	public void registerEthernetCallback(EthernetCallback callback) { 
		this.callback = callback;
	}
	
	public void register(int type, EthernetProcessor processor) {
		Set<EthernetProcessor> processors = callbacks.get(type);
		if (processors == null) {
			processors = new HashSet<EthernetProcessor>();
			callbacks.put(type, processors);
		}

		processors.add(processor);
	}

	public void unregister(int type, EthernetProcessor processor) {
		Set<EthernetProcessor> processors = callbacks.get(type);
		if (processors == null)
			return;

		processors.remove(processor);
	}

	public void decode(PcapPacket packet) {
		// do not reorder following codes (parse sequence)
		MacAddress destination = getMacAddress(packet.getPacketData());
		MacAddress source = getMacAddress(packet.getPacketData());
		int type = getEtherType(packet.getPacketData());
		Buffer buffer = packet.getPacketData();
		buffer.discardReadBytes();

		EthernetFrame frame = new EthernetFrame(source, destination, type, buffer);
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
		//callback.onReceived(frame);
		
		Set<EthernetProcessor> processors = callbacks.get(frame.getType());
		if (processors == null)
			return;

		for (EthernetProcessor processor : processors) {
			processor.process(frame.dup());
		}

		frame = null;
	}
}