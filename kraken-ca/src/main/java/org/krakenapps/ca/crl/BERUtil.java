package org.krakenapps.ca.crl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BERUtil {
	public static byte[] encodingOID(String oid) {
		ByteBuffer bb = ByteBuffer.allocate(20);

		String[] token = oid.split("[.]");
		int oid1 = Integer.parseInt(token[0]);
		int oid2 = Integer.parseInt(token[1]);
		byte b = (byte) (oid1 * 40 + oid2);
		bb.put(b);

		for (int i = 2; i < token.length; i++) {
			int value = Integer.parseInt(token[i]);

			if (value > 0x7F) {
				String binaryString = Integer.toBinaryString(value);
				int lastIndex = binaryString.length() - 1;
				int j = lastIndex;

				List<String> elements = new ArrayList<String>();
				while (j >= 0) {
					String s;
					if (j <= 6)
						s = binaryString.substring(0, j + 1);
					else
						s = binaryString.substring(j - 6, j + 1);

					int mask;
					if (j == lastIndex)
						mask = 0;
					else
						mask = 0x80;

					int result = mask | ((byte) Integer.parseInt(s, 2));
					elements.add(Integer.toBinaryString(result));

					j -= 7;
				}

				for (int k = elements.size() - 1; k >= 0; k--) {
					byte b1 = (byte) Integer.parseInt(elements.get(k), 2);
					bb.put(b1);
				}

			} else {
				bb.put((byte) value);
			}
		}

		int length = bb.position();
		bb.flip();
		byte[] encoded = new byte[length];
		bb.get(encoded);
		return encoded;
	}

	public static byte[] getLengthBytes(int length) {
		if (length > 0x7F) {
			String lenStr = Integer.toHexString(length);
			int lastIndex = lenStr.length() - 1;
			int i = lastIndex;

			List<String> elements = new ArrayList<String>();
			while (i >= 0) {
				if (i == 0) {
					elements.add(lenStr.substring(0, 1));
					break;
				}
				elements.add(lenStr.substring(i - 1, i + 1));
				i -= 2;
			}

			int size = elements.size();
			byte[] b = new byte[1 + size];

			// put number of length bytes(ex. 84 => number of length bytes = 4,
			// 82 => number of length bytes = 2)
			b[0] = (byte) (0x80 | size);

			int k = 1;
			for (int j = size - 1; j >= 0; j--) {
				b[k] = (byte) Integer.parseInt(elements.get(j), 16);
				k++;
			}
			return b;

		} else {
			String lenStr = Integer.toHexString(length);
			return new byte[] { (byte) Integer.parseInt(lenStr, 16) };
		}
	}
}