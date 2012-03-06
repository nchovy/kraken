package org.krakenapps.pcap.decoder.smb.transparser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.transreq.RawWriteNmpipeRequest;
import org.krakenapps.pcap.decoder.smb.transresp.RawWriteNmpipeResponse;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class RawWriteNmpipeParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer , Buffer parameterBuffer , Buffer dataBuffer) {
		RawWriteNmpipeRequest transData = new RawWriteNmpipeRequest();
		transData.setSubcommand(ByteOrderConverter.swap(setupBuffer.getShort()));
		transData.setFid(ByteOrderConverter.swap(setupBuffer.getShort()));
		byte []writeData = new byte[setupBuffer.readableBytes()];
		transData.setWriteData(writeData);
		return transData;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer , Buffer parameterBuffer , Buffer dataBuffer , SmbSession session) {
		RawWriteNmpipeResponse transData = new RawWriteNmpipeResponse();
		if(parameterBuffer.readableBytes() ==0x02){
			transData.setBytesWritten(ByteOrderConverter.swap(parameterBuffer.getShort()));
		}
		return transData;
	}

	
}
