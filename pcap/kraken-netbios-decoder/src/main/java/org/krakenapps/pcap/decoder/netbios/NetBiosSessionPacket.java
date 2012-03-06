package org.krakenapps.pcap.decoder.netbios;

import java.nio.ByteBuffer;

import org.krakenapps.pcap.Injectable;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

public class NetBiosSessionPacket implements Injectable{
	private NetBiosSessionHeader header;
	private NetBiosSessionData data;

	public NetBiosSessionPacket(NetBiosSessionHeader header,
			NetBiosSessionData data) {
		this.header = header;
		this.data = data;
	}

	public NetBiosSessionHeader getHeader() {
		return header;
	}

	public NetBiosSessionData getData() {
		return data;
	}

	@Override
	public String toString() {
		return header.toString() + data.toString();
	}

	@Override
	public Buffer getBuffer() {
		ByteBuffer headerb = ByteBuffer.allocate(4);
	//	ByteBuffer datab = ByteBuffer
		/*header.setType(NetBiosSessionType.parse(b.get() & 0xff));
		header.setFlags(b.get());
		header.setLength(b.getShort());*/
		headerb.put(header.getFlags());
		headerb.putShort(header.getLength());
		
		Buffer buffer = new ChainBuffer();
		buffer.addLast(headerb.array());
		return buffer;
	}
}
