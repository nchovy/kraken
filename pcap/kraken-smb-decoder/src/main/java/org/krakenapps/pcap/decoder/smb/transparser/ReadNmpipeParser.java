package org.krakenapps.pcap.decoder.smb.transparser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.transreq.ReadNmpipeRequest;
import org.krakenapps.pcap.decoder.smb.transresp.ReadNmpipeResponse;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class ReadNmpipeParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer , Buffer parameterBuffer , Buffer dataBuffer) {
		ReadNmpipeRequest transData = new ReadNmpipeRequest();
		transData.setSubcommand(ByteOrderConverter.swap(setupBuffer.getShort()));
		transData.setFid(ByteOrderConverter.swap(setupBuffer.getShort()));
		return transData;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer , Buffer parameterBuffer , Buffer dataBuffer , SmbSession session) {
		ReadNmpipeResponse transData = new ReadNmpipeResponse();
		byte []readData = new byte[setupBuffer.readableBytes()];
		setupBuffer.gets(readData);
		transData.setReadData(readData);
		return null;
	}

}
