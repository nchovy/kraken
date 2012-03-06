package org.krakenapps.pcap.decoder.smb.trans2parser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.trans2req.SetFileInformationRequest;
import org.krakenapps.pcap.decoder.smb.trans2resp.SetFileInformationResponse;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SetFileInformationParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer  , Buffer parameterBuffer , Buffer dataBuffer) {
		SetFileInformationRequest transData = new SetFileInformationRequest();
		transData.setSubcommand(ByteOrderConverter.swap(setupBuffer.getShort()));
		transData.setFid(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setInformaiotnLevel(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setReserved(ByteOrderConverter.swap(parameterBuffer.getShort()));
		//TODO : dataBuffer parsing depend on information level
		return transData;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer  , Buffer parameterBuffer , Buffer dataBuffer, SmbSession session) {
		SetFileInformationResponse transData = new SetFileInformationResponse();
		transData.setEaErrorOffset(ByteOrderConverter.swap(parameterBuffer.getShort()));
		return transData;
	}

}
