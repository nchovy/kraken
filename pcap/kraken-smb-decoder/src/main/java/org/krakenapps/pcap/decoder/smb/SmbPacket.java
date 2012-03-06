package org.krakenapps.pcap.decoder.smb;

import org.krakenapps.pcap.Injectable;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;

public class SmbPacket implements Injectable {
	public SmbHeader header;
	public SmbData data;

	public String toString() {
		return header.toString() + data.toString();
	}

	@Override
	public Buffer getBuffer() {
		// TODO Auto-generated method stub
		return null;
	}
}
