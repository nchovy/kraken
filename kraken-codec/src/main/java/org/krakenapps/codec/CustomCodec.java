package org.krakenapps.codec;

import java.nio.ByteBuffer;

public interface CustomCodec {
	void encode(ByteBuffer bb, Object value);

	int lengthOf(Object value);

	Object decode(ByteBuffer bb);
}
