package org.krakenapps.pcap.decoder.netbios;

import org.krakenapps.pcap.util.Buffer;

public class NetBiosNameService {
	public NetBiosNamePacket parse(Buffer b) {
		NetBiosNameHeader header = NetBiosNameHeader.parse(b);
		NetBiosNameData data = NetBiosNameData.parse(header, b);
		return new NetBiosNamePacket(header, data);
	}
}
