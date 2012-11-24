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
package org.krakenapps.logdb.sort;

import java.nio.ByteBuffer;

import org.krakenapps.codec.CustomCodec;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.codec.UnsupportedTypeException;

public class SortCodec implements CustomCodec {
	public final static SortCodec instance = new SortCodec();

	@Override
	public void encode(ByteBuffer bb, Object value) {
		if (value.getClass() != Item.class)
			throw new UnsupportedTypeException(value.toString());

		Item item = (Item) value;
		int contentLength = lengthOf(item);

		bb.put((byte) 145);
		EncodingRule.encodeRawNumber(bb, int.class, contentLength);
		EncodingRule.encode(bb, item.getKey());
		EncodingRule.encode(bb, item.getValue());
	}

	@Override
	public int lengthOf(Object value) {
		if (value.getClass() != Item.class)
			throw new UnsupportedTypeException(value.toString());

		Item item = (Item) value;
		int contentLength = EncodingRule.lengthOf(item.getKey()) + EncodingRule.lengthOf(item.getValue());
		return 1 + EncodingRule.lengthOfRawNumber(int.class, contentLength) + contentLength;
	}

	@Override
	public Object decode(ByteBuffer bb) {
		int begin = bb.position();
		bb.get(); // read type (1 byte)
		int length = (int) EncodingRule.decodeRawNumber(bb);
		int limit = bb.limit();
		int endPos = begin + length;
		bb.limit(endPos);

		Object key = EncodingRule.decode(bb);
		Object value = EncodingRule.decode(bb);

		bb.limit(limit);

		return new Item(key, value);
	}

	@Override
	public int getObjectLength(ByteBuffer bb) {
		throw new UnsupportedOperationException();
	}

}
