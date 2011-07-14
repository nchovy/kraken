package org.krakenapps.ber;

import java.nio.ByteBuffer;

public class OIDParser {
	private OIDParser() {
	}
	
	public static String parse(byte[] oidBytes) {
		if (oidBytes[0] != 0x06)
			/* must not access this case */
			return null;

		ByteBuffer buffer = ByteBuffer.allocate(100);
		buffer.mark();
		int length = oidBytes[1];

		/* get OID */
		int i = 0;
		while (i < length) {
			int val = oidBytes[i + 2];
			if (i + 2 == 2) {
				buffer.putInt((int) (val / 40));
				buffer.putInt((int) (val % 40));
				i++;
			} else {
				int index = i + 2;

				/* reference: http://www.rane.com/note161.html */
				if ((val & 0x80) == 0x80) {
					/* OID value: consist of multiple bytes */
					do {
						val = oidBytes[++index];
					} while ((val & 0x80) == 0x80);

					val = 0;
					int maxExponent = index - (i + 2);
					index = i + 2;
					for (int j = 0; j <= maxExponent; j++) {
						if (j == maxExponent) {
							val += oidBytes[index];
							break;
						}
						val += ((oidBytes[index] & 0x7f) * Math.pow(128, maxExponent - j));
						index++;
					}
					buffer.putInt(val);
					i++;
					i += maxExponent;
				} else {
					/* OID value: 1 byte */
					buffer.putInt((int) val);
					i++;
				}
			}
		}
		buffer.putInt(-1);
		buffer.reset();

		StringBuilder builder = new StringBuilder();
		while (true) {
			int num = buffer.getInt();
			if (num == -1)
				break;

			builder.append(num);
			builder.append(".");
		}
		/* remove last dot */
		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}
}