package org.krakenapps.ber;

import org.krakenapps.pcap.util.Buffer;

public class BERDecoder {
	private BERDecoder() {
	}

	public static BERObject getBERObject(Buffer buffer) {
		byte b = buffer.get();
		int type = (int) (b & 0x1f);
		int length = buffer.get();

		if ((length & 0x80) == 0x80) {
			int numOfLengthByte = length & 0x0f;
			int loopCount = numOfLengthByte;
			int tempLength = 0;

			while (loopCount > 0) {
				byte b1 = buffer.get();
				tempLength = (tempLength << 8) | (b1 & 0xff);
				loopCount--;
			}

			switch (numOfLengthByte) {
			case 1:
				length = tempLength & 0xff;
				break;
			case 2:
				length = tempLength & 0xffff;
				break;
			case 3:
				length = tempLength & 0xffffff;
				break;
			case 4:
				length = tempLength;
				break;
			default:
				/* must not access this case */
				length = 0;
				break;
			}
		}

		byte[] data = new byte[length];
		buffer.mark();
		buffer.gets(data);
		buffer.reset();

		return new BERObject(type, length, data);
	}
}