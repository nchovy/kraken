package org.krakenapps.pcap.decoder.smb.trans2parser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.trans2req.SetFsInformationRequest;
import org.krakenapps.pcap.decoder.smb.trans2resp.SetFsInformationResponse;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;
import org.krakenapps.pcap.util.Buffer;

public class SetFsInformationParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer  , Buffer parameterBuffer , Buffer dataBuffer) {
		SetFsInformationRequest transData = new SetFsInformationRequest();
		transData.setFid(parameterBuffer.getShort());
		transData.setInformatoinLevel(parameterBuffer.getShort());
		// TODO Auto-generated method stub
		return transData;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer  , Buffer parameterBuffer , Buffer dataBuffer , SmbSession session) {
		SetFsInformationResponse transData = new SetFsInformationResponse();
		// there is no parameters and data
		// TODO Auto-generated method stub
		return transData;
	}

}
