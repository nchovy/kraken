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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.packet.PcapPacket;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

/**
 * @author mindori
 */
public class IpReassembler {
	private Map<Integer, HoleManagerWrapper> lmap;
	private PriorityQueue<HoleManagerWrapper> wrapper;
	private int dropTimeout;

	public IpReassembler() {
		this.lmap = new HashMap<Integer, HoleManagerWrapper>();
		this.wrapper = new PriorityQueue<IpReassembler.HoleManagerWrapper>(11, new HoleManagerWrapperComparator());
		this.dropTimeout = 30000;
	}

	public int getDropTimeout() {
		return dropTimeout;
	}

	public void setDropTimeout(int dropTimeout) {
		this.dropTimeout = dropTimeout;
	}

	public Ipv4Packet tryReassemble(Ipv4Packet fragment) {
		/* check HoleDescriptor */
		int id = fragment.getId();
		if (!lmap.containsKey(id)) {
			HoleManagerWrapper t = new HoleManagerWrapper(id, new HoleManager());
			lmap.put(id, t);
			wrapper.add(t);
		}

		HoleManagerWrapper t = lmap.get(id);
		t.setTime(((EthernetFrame) fragment.getL2Frame()).getPcapPacket());
		HoleManager h = t.getManager();
		int offset = fragment.getFragmentOffset() * 8;
		int length = fragment.getTotalLength() - fragment.getIhl();
		Buffer data = fragment.getData();

		/* check MF == 0 */
		if (fragment.getFlags() == 0) {
			int goal = offset + length;
			h.setGoal(goal);
		}

		/* put data */
		h.put(data, offset, length);

		/* check flush */
		if (h.isFlush(offset, length)) {
			h.flush(data);
		} else
			return null;

		if (h.isReassemble()) {
			ByteBuffer b = h.getReassembled();
			b.position(0);
			return reassemble(fragment, b, h.getGoal());
		} else
			return null;
	}

	public void drop() {
		long now = System.currentTimeMillis();
		while (!wrapper.isEmpty() && now - wrapper.peek().getTime() > dropTimeout)
			lmap.remove(wrapper.poll().getId());
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

	private class HoleManagerWrapper {
		private int id;
		private HoleManager manager;
		private long time;

		public HoleManagerWrapper(int id, HoleManager manager) {
			this.id = id;
			this.manager = manager;
		}

		public int getId() {
			return id;
		}

		public long getTime() {
			return time;
		}

		public void setTime(PcapPacket packet) {
			this.time = ((long) (packet.getPacketHeader().getTsSec())) * 1000;
			this.time += packet.getPacketHeader().getTsUsec() / 1000;
		}

		public HoleManager getManager() {
			return manager;
		}
	}

	private class HoleManagerWrapperComparator implements Comparator<HoleManagerWrapper> {
		@Override
		public int compare(HoleManagerWrapper o1, HoleManagerWrapper o2) {
			return (int) (o1.getTime() - o2.getTime());
		}
	}
}
