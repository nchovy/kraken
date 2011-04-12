package org.krakenapps.pcap.decoder.smb.transparser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.transreq.RawReadNmpipeRequest;
import org.krakenapps.pcap.decoder.smb.transresp.RawReadNmpipeResponse;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class RawReadNmpipeParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer , Buffer parameterBuffer , Buffer dataBuffer) {
		RawReadNmpipeRequest transData = new RawReadNmpipeRequest();
		transData.setSubCommand(ByteOrderConverter.swap(setupBuffer.getShort()));
		transData.setFid(ByteOrderConverter.swap(setupBuffer.getShort()));
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer , Buffer parameterBuffer , Buffer dataBuffer, SmbSession session) {
		RawReadNmpipeResponse transData = new RawReadNmpipeResponse();
		byte []byteRead = new byte[setupBuffer.readableBytes()];
		setupBuffer.gets(byteRead);
		transData.setByteRead(byteRead);
		// TODO Auto-generated method stub
		return null;
	}

}
