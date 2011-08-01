package org.krakenapps.codec;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.WeakHashMap;

public class EncodedStringCache {
	private static Map<String, EncodedStringCache> cache = new WeakHashMap<String, EncodedStringCache>();
	private byte[] value;
	private int rawNumberLength;

	public static EncodedStringCache getEncodedString(String value) {
		EncodedStringCache es = cache.get(value);

		if (es == null) {
			es = new EncodedStringCache(value);
			cache.put(new String(value), es);
		}

		return es;
	}

	private EncodedStringCache(String value) {
		try {
			this.value = value.getBytes("utf-8");
			this.rawNumberLength = EncodingRule.lengthOfRawNumber(int.class, this.value.length);
		} catch (UnsupportedEncodingException e) {
		}
	}

	public int length() {
		return 1 + rawNumberLength + value.length;
	}

	public byte[] value() {
		return value;
	}
}
