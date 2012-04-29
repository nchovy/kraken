package org.krakenapps.btree;

import java.nio.ByteBuffer;

public class NumberEncoder {
	public static int encode(ByteBuffer bb, long value) {
		int len = lengthOf(value);
		for (int i = 0; i < len; ++i) {
			byte signalBit = (byte) (i != len - 1 ? 0x80 : 0);
			byte data = (byte) (signalBit | (byte) (value >> (7 * (len - i - 1)) & 0x7F));
			bb.put(data);
		}
		return len;
	}

	public static long decode(ByteBuffer bb) {
		long value = 0L;

		byte b;
		do {
			value = value << 7;
			b = bb.get();
			value |= b & 0x7F;
		} while ((b & 0x80) == 0x80);
		return value;
	}
	
	public static int toZNumber(short value) {
		return (value << 1) ^ (value >> 15);
	}

	public static long toZNumber(long value) {
		return (value << 1) ^ (value >> 63);
	}

	public static int toZNumber(int value) {
		return (value << 1) ^ (value >> 31);
	}

	@Deprecated
	private static int lengthOfRawNumber2(long value) {
		int byteCount = 0;
		if (value == 0)
			return 1;
		
		if (value < 0)
			return 10; // max value
		
		long val2 = value;
		while (val2 != 0) {
			val2 = val2 >> 7;
			byteCount++;
		}
		return byteCount;
	}
	
	public static <T> int lengthOf(long value) {
		if (value == 0)
			return 1;

		return (63 - Long.numberOfLeadingZeros(value)) / 7 + 1;
	}
	
	public static void main(String[] args) {
		System.out.println("adsf");
		System.out.println(lengthOf(-1));
		System.out.println(lengthOf((0x1 << 7) - 1));
		System.out.println(lengthOf((0x1 << 7) - 2));
		System.out.println(lengthOf(0x1 << 7));
		System.out.println(lengthOf(0x1 << 14 - 1));
		System.out.println(lengthOf(0x1 << 14));
		System.out.println(lengthOf(0x1 << 21 - 1));
		System.out.println(lengthOf(0x1 << 21));
		System.out.println(lengthOf(Long.MAX_VALUE));
		System.out.println(lengthOf(Long.MIN_VALUE));
		System.out.println("---");		
		System.out.println(lengthOfRawNumber2(-1));
		System.out.println(lengthOfRawNumber2((0x1 << 7) - 1));
		System.out.println(lengthOfRawNumber2((0x1 << 7) - 2));
		System.out.println(lengthOfRawNumber2(0x1 << 7));
		System.out.println(lengthOfRawNumber2(0x1 << 14 - 1));
		System.out.println(lengthOfRawNumber2(0x1 << 14));
		System.out.println(lengthOfRawNumber2(0x1 << 21 - 1));
		System.out.println(lengthOfRawNumber2(0x1 << 21));
		System.out.println(lengthOfRawNumber2(Long.MAX_VALUE));
		System.out.println(lengthOfRawNumber2(Long.MIN_VALUE));

		System.out.println("---");
		
		System.out.println(Integer.MIN_VALUE);
		System.out.println(toZNumber(Integer.MIN_VALUE) & -1L);
		
		System.out.println("---");

		long start = 0;
		
		if (lengthOf(Integer.MAX_VALUE) != lengthOfRawNumber2(Integer.MAX_VALUE)) {
			System.out.println("ERROR!");
		}
		
		start = System.currentTimeMillis();
		for (int i = 0; i < 100000000; ++i) {
			lengthOf(Integer.MAX_VALUE);			
		}
		System.out.println(System.currentTimeMillis() - start);
		start = System.currentTimeMillis();
		for (int i = 0; i < 100000000; ++i) {
			lengthOfRawNumber2(Integer.MAX_VALUE);		
		}
		System.out.println(System.currentTimeMillis() - start);
	}
}
