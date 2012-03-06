package org.krakenapps.pcap.decoder.smb.transparser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.transreq.WriteNmpipeRequest;
import org.krakenapps.pcap.decoder.smb.transresp.WriteNmpipeResponse;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class WriteNmpipeParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer , Buffer parameterBuffer , Buffer dataBuffer) {
		WriteNmpipeRequest transData = new WriteNmpipeRequest();
		transData.setSubcommand(ByteOrderConverter.swap(setupBuffer.getShort()));
		transData.setFid(ByteOrderConverter.swap(setupBuffer.getShort()));
		byte []writeData = new byte[setupBuffer.readableBytes()];
		setupBuffer.gets(writeData);
		transData.setWriteData(writeData);
		return transData;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer , Buffer parameterBuffer , Buffer dataBuffer , SmbSession session) {
		WriteNmpipeResponse transData = new WriteNmpipeResponse();
		transData.setBytesWritten(ByteOrderConverter.swap(setupBuffer.getShort()));
		return transData;
	}

}
