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
package org.krakenapps.tftp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class TftpClient {
	public void get(InetSocketAddress target, TftpMode mode, String source, String destination) throws IOException,
			FileNotFoundException {
		byte[] b = firstPacket((short) 1, mode, source);
		ClientGetProcessor processor = new ClientGetProcessor();
		processor.start(target, b, source, destination);
	}

	public void put(InetSocketAddress target, TftpMode mode, String source, String destination) throws IOException,
			FileNotFoundException {
		byte[] b;
		
		if (destination == null) {
			int pos = source.lastIndexOf("/");
			if (pos == -1)
				b = firstPacket((short) 2, mode, source);
			else
				b = firstPacket((short) 2, mode, source.substring(pos + 1));
		} else
			b = firstPacket((short) 2, mode, destination);
		
		ClientPutProcessor processor = new ClientPutProcessor();
		processor.start(target, b, source);
	}

	private byte[] firstPacket(short opCode, TftpMode mode, String fileName) {
		ByteBuffer bb = ByteBuffer.allocate(272);
		bb.putShort(opCode);
		bb.put(fileName.getBytes());
		bb.put((byte) 0x00);

		switch (mode) {
		case NETASCII:
			bb.put(new byte[] { 0x6e, 0x65, 0x74, 0x61, 0x73, 0x63, 0x69, 0x69, 0x00 });
			break;
		case OCTET:
			bb.put(new byte[] { 0x6f, 0x63, 0x74, 0x65, 0x74, 0x00 });
			break;
		case MAIL:
			bb.put(new byte[] { 0x6d, 0x61, 0x69, 0x6c, 0x00 });
			break;
		}

		bb.flip();
		int length = bb.limit();
		byte[] b = new byte[length];
		bb.get(b);
		return b;
	}
}