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
package org.krakenapps.pcap.util;

import static org.krakenapps.pcap.util.PacketManipulator.*;

import java.io.IOException;

import org.krakenapps.pcap.Injectable;
import org.krakenapps.pcap.PacketBuilder;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;

public class WakeOnLan {
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
