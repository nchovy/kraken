package org.krakenapps.pcap.decoder.netbios.rr;

import org.krakenapps.pcap.decoder.netbios.NetBiosSessionData;
import org.krakenapps.pcap.util.Buffer;

public class SessionNegativeResponse implements NetBiosSessionData {
	private byte errorCode; // only use Negative session response packet

	// this use retarget response packet only
	private SessionNegativeResponse() {
	}

	public byte getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(byte errorCode) {
		this.errorCode = errorCode;
	}

	public static NetBiosSessionData parse(Buffer b) {
		SessionNegativeResponse data = new SessionNegativeResponse();
		data.setErrorCode(b.get());
		return data;
	}

	@Override
	public String toString() {
		return String.format("netbios negative response: error code=%x", this.errorCode);
	}

	@Override
	public Buffer getBuffer() {
		// TODO Auto-generated method stub
		return null;
	}
}
