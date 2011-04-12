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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

/**
 * @author mindori
 */
public class IpReassembler {
	private Map<Integer, HoleManager> lmap;
	
	public IpReassembler() { 
		lmap = new HashMap<Integer, HoleManager>();	
	}
	
	public Ipv4Packet tryReassemble(Ipv4Packet fragment) { 
		/* check HoleDescriptor */
		int id = fragment.getId();
		if(!lmap.containsKey(id)) {
			lmap.put(id, new HoleManager());
		}
		
		HoleManager h = lmap.get(id);
		int offset = fragment.getFragmentOffset() * 8;
		int length = fragment.getTotalLength() - fragment.getIhl();
		Buffer data = fragment.getData();
		
		/* check MF == 0 */
		if(fragment.getFlags() == 0) {
			int goal = offset + length;
			h.setGoal(goal);
		}

		/* put data */
		h.put(data, offset, length);
		
		/* check flush */
		if(h.isFlush(offset, length)) {
			h.flush(data);
		}
		else 
			return null;
		
		if(h.isReassemble()) { 
			ByteBuffer b = h.getReassembled();
			b.position(0);
			return reassemble(fragment, b, h.getGoal());
		}
		else 
			return null;
	}
	
	private Ipv4Packet reassemble(Ipv4Packet fragment, ByteBuffer reassembled, int goal) {
		byte[] b = new byte[goal];
		reassembled.get(b, 0, goal);
		
		Buffer data = new ChainBuffer();
		data.addLast(b);
		
		int tl = fragment.getIhl() + goal;
		Ipv4Packet p = Ipv4Packet.makeReassembled(fragment, data, tl);
		
		return p;
	}
}
