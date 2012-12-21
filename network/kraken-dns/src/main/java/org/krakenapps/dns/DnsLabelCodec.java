package org.krakenapps.dns;

import java.nio.ByteBuffer;
import java.util.StringTokenizer;

public class DnsLabelCodec {
	private DnsLabelCodec() {
	}

	/**
	 * parse domain name (label sequence) from dns message
	 * 
	 * @param msg
	 *            whole dns message binary (including header)
	 * @param offset
	 *            current label offset to parse
	 * @return the label
	 */
	public static String decode(ByteBuffer bb) {
		boolean first = true;
		StringBuilder sb = new StringBuilder();

		while (true) {
			/**
			 * check msb 2 bit for decompression. label length is represented by
			 * 6 bit, therefore max 64.
			 */
			byte b = bb.get();
			if (b == 0)
				break;

			if ((b & 0xc0) != 0) {
				int nextOffset = b & 0x3f;
				b = bb.get();
				nextOffset = (nextOffset << 8) | (b & 0xff);
				ByteBuffer dup = bb.duplicate();
				dup.position(nextOffset);
				String prefix = sb.toString();
				if (prefix.isEmpty())
					return decode(dup);
				else
					return prefix + "." + decode(dup);
			}

			int len = b & 0x3f;

			byte[] label = new byte[len];
			bb.get(label);

			if (first)
				first = false;
			else
				sb.append(".");

			sb.append(new String(label));
		}

		return sb.toString();
	}

	public static void encode(ByteBuffer bb, String domain) {
		// TODO: support message compression using label table parameter

		StringTokenizer tok = new StringTokenizer(domain, ".");
		while (tok.hasMoreTokens()) {
			byte[] label = tok.nextToken().getBytes();
			if (label.length >= 64)
				throw new IllegalArgumentException("domain label length should be less than 64 bytes (see RFC)");

			bb.put((byte) label.length);
			bb.put(label);
		}

		bb.put((byte) 0);
	}
}
