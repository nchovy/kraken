package org.krakenapps.pcap.decoder.smb.comparser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;

public interface SmbDataParser {
	SmbData parseRequest(SmbHeader h, Buffer b ,  SmbSession session);
//	SmbData parseRequest(SmbHeader h, Buffer b);
	SmbData parseResponse(SmbHeader h, Buffer b , SmbSession session);
}
