package org.krakenapps.pcap.decoder.netbios.rr;

import org.krakenapps.pcap.decoder.netbios.DatagramData;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.util.Buffer;

public class DirectBroadcastData implements DatagramData {
	private String sourceName;
	private String DestName;
	private Buffer userData;
	private byte domainType;
	
	private DirectBroadcastData() {

	}
	
	public byte getDomainType() {
		return domainType;
	}

	public void setDomainType(byte domainType) {
		this.domainType = domainType;
	}

	public String getSoruceName() {
		return sourceName;
	}

	public void setSoruceName(String soruceName) {
		this.sourceName = soruceName;
	}

	public String getDestName() {
		return DestName;
	}

	public void setDestName(String destName) {
		DestName = destName;
	}

	public Buffer getUserData() {
		return userData;
	}

	public void setUserData(Buffer userData) {
		this.userData = userData;
	}

	public static DirectBroadcastData parse(Buffer b) {
		DirectBroadcastData data = new DirectBroadcastData();
		data.setDomainType(NetBiosNameCodec.decodeDomainType(b));
		data.setSoruceName(NetBiosNameCodec.readName(b));
		data.setDestName(NetBiosNameCodec.readName(b));
		data.userData = b;
		return data;
	}

	@Override
	public String toString() {
		return String.format("DatagramData DirectBroadcastData\n"+
				"sourceName=%s, DestName=%s\n", sourceName, DestName);
	}
}
