package org.krakenapps.pcap.decoder.smb.ntparser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;
import org.krakenapps.pcap.util.Buffer;

public class NtTransactSetQuotaParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer , Buffer parameterBuffer,  Buffer dataBuffer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer ,Buffer parameterBuffer,  Buffer dataBuffer , SmbSession session) {
		// TODO Auto-generated method stub
		return null;
	}

}
