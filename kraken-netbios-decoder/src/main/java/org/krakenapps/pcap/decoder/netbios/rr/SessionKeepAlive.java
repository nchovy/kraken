package org.krakenapps.pcap.decoder.netbios.rr;

import org.krakenapps.pcap.decoder.netbios.NetBiosSessionData;
import org.krakenapps.pcap.util.Buffer;

public class SessionKeepAlive implements NetBiosSessionData {
	private SessionKeepAlive() {
	}

	public static NetBiosSessionData parse(Buffer b) {
		return new SessionKeepAlive();
	}

	@Override
	public String toString() {
		return String.format("netbios: keepalive");
	}

	@Override
	public Buffer getBuffer() {
		// TODO Auto-generated method stub
		return null;
	}
}
