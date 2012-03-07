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
package org.krakenapps.pcap.decoder.snmp;

import java.util.HashSet;
import java.util.Set;

import org.krakenapps.pcap.decoder.snmp.types.Integer32;
import org.krakenapps.pcap.decoder.snmp.types.Sequence;
import org.krakenapps.pcap.decoder.snmp.types.Variable;
import org.krakenapps.pcap.decoder.snmp.v2.Pdu;
import org.krakenapps.pcap.decoder.udp.UdpPacket;
import org.krakenapps.pcap.decoder.udp.UdpProcessor;
import org.krakenapps.pcap.util.Buffer;

public class SnmpDecoder implements UdpProcessor {
	private Set<SnmpV1Processor> v1Callbacks;
	private Set<SnmpV2Processor> v2Callbacks;
	private Set<SnmpV3Processor> v3Callbacks;

	public SnmpDecoder() {
		v1Callbacks = new HashSet<SnmpV1Processor>();
		v2Callbacks = new HashSet<SnmpV2Processor>();
		v3Callbacks = new HashSet<SnmpV3Processor>();
	}

	public void register(SnmpV1Processor processor) {
		v1Callbacks.add(processor);
	}

	public void register(SnmpV2Processor processor) {
		v2Callbacks.add(processor);
	}

	public void register(SnmpV3Processor processor) {
		v3Callbacks.add(processor);
	}

	public void unregister(SnmpV1Processor processor) {
		v1Callbacks.remove(processor);
	}

	public void unregister(SnmpV2Processor processor) {
		v2Callbacks.remove(processor);
	}

	public void unregister(SnmpV3Processor processor) {
		v3Callbacks.remove(processor);
	}

	@Override
	public void process(UdpPacket p) {
		Buffer b = p.getData();
		int remain = b.readableBytes();
		byte[] data = new byte[remain];
		b.gets(data, 0, remain);
		
		Sequence seq = (Sequence) Variable.decode(data, 0, 0);
		int version = (int) ((Integer32) seq.get(0)).get();
		switch (version) {
		case 0: {
			org.krakenapps.pcap.decoder.snmp.v1.Pdu pdu = org.krakenapps.pcap.decoder.snmp.v1.Pdu.parse(seq);
			for (SnmpV1Processor callback : v1Callbacks)
				callback.onReceive(p.getSource(), p.getDestination(), pdu);
		}
			break;
		case 1: {
			Pdu pdu = Pdu.parse(seq);
			for (SnmpV2Processor callback : v2Callbacks)
				callback.onReceive(p.getSource(), p.getDestination(), pdu);
		}
			break;
		case 2:
			break;
		}

	}
}