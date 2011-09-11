/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.webconsole.impl;

import java.util.Arrays;
import java.util.UUID;

import org.jboss.netty.buffer.ChannelBuffer;

public class ByteUtil {
	public static byte[] asArray(ChannelBuffer c) {
		return Arrays.copyOf(c.array(), c.readableBytes());
	}

	public static byte[] asByteArray(UUID uuid) {
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		byte[] buffer = new byte[16];

		for (int i = 0; i < 8; i++)
			buffer[i] = (byte) (msb >>> 8 * (7 - i));
		for (int i = 8; i < 16; i++)
			buffer[i] = (byte) (lsb >>> 8 * (7 - i));

		return buffer;
	}
}
