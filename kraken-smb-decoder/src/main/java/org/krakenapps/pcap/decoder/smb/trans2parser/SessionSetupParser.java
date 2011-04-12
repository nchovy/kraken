package org.krakenapps.pcap.decoder.smb.trans2parser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.trans2req.SessionSetupRequest;
import org.krakenapps.pcap.decoder.smb.trans2resp.SessionSetupResponse;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;
import org.krakenapps.pcap.util.Buffer;

public class SessionSetupParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer  , Buffer parameterBuffer , Buffer dataBuffer) {
		SessionSetupRequest transData = new SessionSetupRequest();
		// TODO Auto-generated method stub
		return transData;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer  , Buffer parameterBuffer , Buffer dataBuffer , SmbSession session) {
		SessionSetupResponse transData = new SessionSetupResponse();
		// TODO Auto-generated method stub
		return transData;
	}

}
