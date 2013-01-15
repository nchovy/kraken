/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.codec;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.WeakHashMap;

public class EncodedStringCache {
	private static Map<String, EncodedStringCache> cache = new WeakHashMap<String, EncodedStringCache>();
	private byte[] value;
	private int rawNumberLength;

	public synchronized static EncodedStringCache getEncodedString(String value) {
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
