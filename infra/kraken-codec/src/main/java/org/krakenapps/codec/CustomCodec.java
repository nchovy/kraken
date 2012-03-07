package org.krakenapps.codec;

import java.nio.ByteBuffer;

public interface CustomCodec {
	void encode(ByteBuffer bb, Object value);

	int lengthOf(Object value);
	
	int getObjectLength(ByteBuffer bb);

	Object decode(ByteBuffer bb);
}
