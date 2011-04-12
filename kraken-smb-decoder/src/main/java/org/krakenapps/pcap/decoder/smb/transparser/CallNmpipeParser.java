package org.krakenapps.pcap.decoder.smb.transparser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.transreq.CallNmpipeRequest;
import org.krakenapps.pcap.decoder.smb.transresp.CallNmpipeResponse;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class CallNmpipeParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer , Buffer parameterBuffer , Buffer dataBuffer) {
		CallNmpipeRequest transData = new CallNmpipeRequest();
		transData.setSubcommand(ByteOrderConverter.swap(setupBuffer.getShort()));
		transData.setPriority(ByteOrderConverter.swap(setupBuffer.getShort()));
		byte []writeData = new byte[dataBuffer.readableBytes()];
		dataBuffer.gets(writeData);
		transData.setWriteData(writeData);
		return transData;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer , Buffer parameterBuffer , Buffer dataBuffer , SmbSession session) {
		CallNmpipeResponse transData = new CallNmpipeResponse();
		byte []readData = new byte[setupBuffer.readableBytes()];
		transData.setReadData(readData);
		return transData;
	}
}
