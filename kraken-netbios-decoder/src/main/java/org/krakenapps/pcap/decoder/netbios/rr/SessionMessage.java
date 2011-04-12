package org.krakenapps.pcap.decoder.netbios.rr;

import org.krakenapps.pcap.decoder.netbios.NetBiosSessionData;
import org.krakenapps.pcap.util.Buffer;

public class SessionMessage implements NetBiosSessionData {
	private Buffer buffer; // this use session message only

	private SessionMessage(Buffer buffer) {
		this.buffer = buffer;
	}

	public static NetBiosSessionData parse(Buffer b) {
		return new SessionMessage(b);
	}

	@Override
	public String toString() {
		return String.format("netbios session: message buffer length=%d", buffer.readableBytes());
	}

	@Override
	public Buffer getBuffer() {
		// TODO Auto-generated method stub
		return buffer;
	}
}
