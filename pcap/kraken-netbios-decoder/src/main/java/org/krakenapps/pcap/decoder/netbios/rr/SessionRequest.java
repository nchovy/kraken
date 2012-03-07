package org.krakenapps.pcap.decoder.netbios.rr;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.netbios.NetBiosSessionData;
import org.krakenapps.pcap.util.Buffer;

public class SessionRequest implements NetBiosSessionData {
	private String calledName;
	private String callingName;
	private byte domainType;
	private SessionRequest(String calledName, String callingName) {
		this.calledName = calledName;
		this.callingName = callingName;
	}
	private SessionRequest(String calledName, String callingName , byte domainType) {
		this.calledName = calledName;
		this.callingName = callingName;
		this.domainType = domainType;
	}
	public byte getDomainType() {
		return domainType;
	}
	public void setDomainType(byte domainType) {
		this.domainType = domainType;
	}
	public String getCalledName() {
		return calledName;
	}

	public String getCallingName() {
		return callingName;
	}

	public static NetBiosSessionData parse(Buffer b) {
		byte domainType = NetBiosNameCodec.decodeDomainType(b);
		String calledName = NetBiosNameCodec.readName(b);
		String callingName = NetBiosNameCodec.readName(b);
		return new SessionRequest(calledName, callingName , domainType);
	}

	@Override
	public String toString() {
		return String.format("NetBiosSessionData SessionRequest" +
				"calledName=%s , callingName=%s", this.calledName, this.callingName);
	}
	@Override
	public Buffer getBuffer() {
		// TODO Auto-generated method stub
		return null;
	}
}
