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
