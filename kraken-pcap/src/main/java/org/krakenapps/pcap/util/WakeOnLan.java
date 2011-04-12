package org.krakenapps.pcap.util;

import static org.krakenapps.pcap.util.PacketManipulator.*;

import java.io.IOException;

import org.krakenapps.pcap.Injectable;
import org.krakenapps.pcap.PacketBuilder;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;

public class WakeOnLan {
	public static void main(String[] args) throws IOException {
		System.loadLibrary("kpcap");
		wake(new MacAddress("00:24:21:b3:43:c1"));
	}
	
	public static void wake(MacAddress target) throws IOException {
		broadcast(ETH().dst(target).type(0x0842).data(new WakeOnLanBuilder(target)));
	}

	private WakeOnLan() {
	}

	private static class WakeOnLanBuilder implements PacketBuilder {
		private MacAddress target;

		public WakeOnLanBuilder(MacAddress target) {
			this.target = target;
		}

		@Override
		public Injectable build() {
			byte ff = (byte) 0xff;
			byte[] b = target.getBytes();

			final Buffer buf = new ChainBuffer(new byte[] { ff, ff, ff, ff, ff, ff });
			for (int i = 0; i < 16; i++)
				buf.addLast(b);

			return new Injectable() {
				@Override
				public Buffer getBuffer() {
					return buf;
				}
			};
		}

		@Override
		public Object getDefault(String name) {
			return null;
		}

	}
}
