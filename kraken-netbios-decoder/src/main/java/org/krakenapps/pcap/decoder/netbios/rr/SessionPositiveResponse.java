package org.krakenapps.pcap.decoder.netbios.rr;

import org.krakenapps.pcap.decoder.netbios.NetBiosSessionData;
import org.krakenapps.pcap.util.Buffer;

public class SessionPositiveResponse implements NetBiosSessionData {
	private SessionPositiveResponse() {
	}

	public static NetBiosSessionData parse(Buffer b) {
		return new SessionPositiveResponse();
	}

	@Override
	public String toString() {
		return String.format("netbios session: positive response");
	}

	@Override
	public Buffer getBuffer() {
		// TODO Auto-generated method stub
		return null;
	}
}
