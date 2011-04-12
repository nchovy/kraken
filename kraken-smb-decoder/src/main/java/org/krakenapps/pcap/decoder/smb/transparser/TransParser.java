package org.krakenapps.pcap.decoder.smb.transparser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.util.Buffer;


public interface TransParser {
	public TransData parseRequest(Buffer setupBuffer , Buffer parameterBuffer , Buffer DataBuffer);
	public TransData parseResponse(Buffer setupBuffer , Buffer parameterBuffer , Buffer DataBuffer , SmbSession session);
}
