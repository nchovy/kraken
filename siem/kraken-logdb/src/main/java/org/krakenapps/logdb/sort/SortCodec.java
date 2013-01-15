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

import org.krakenapps.codec.BinaryForm;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.codec.FastCustomCodec;
import org.krakenapps.codec.FastEncodingRule;
import org.krakenapps.codec.UnsupportedTypeException;

public class SortCodec implements FastCustomCodec {
	public final static SortCodec instance = new SortCodec();

	@Override
	public BinaryForm preencode(FastEncodingRule enc, Object value) {
		if (value.getClass() != Item.class)
			throw new UnsupportedTypeException(value.toString());

		BinaryForm bf = new BinaryForm();
		Item item = (Item) value;

		BinaryForm keyBinary = enc.preencode(item.getKey());
		BinaryForm valueBinary = enc.preencode(item.getValue());
		int payloadLength = keyBinary.totalLength + valueBinary.totalLength;

		bf.type = 145;
		bf.children = new BinaryForm[2];
		bf.children[0] = keyBinary;
		bf.children[1] = valueBinary;
		bf.lengthBytes = enc.encodeRawNumber(int.class, payloadLength);
		bf.totalLength = 1 + bf.lengthBytes.length + payloadLength;
		bf.value = value;
		return bf;
	}

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
		bb.get(); // read type (1 byte)
		EncodingRule.decodeRawNumber(bb);
		Object key = EncodingRule.decode(bb);
		Object value = EncodingRule.decode(bb);
		return new Item(key, value);
	}

	@Override
	public int getObjectLength(ByteBuffer bb) {
		throw new UnsupportedOperationException();
	}

}
