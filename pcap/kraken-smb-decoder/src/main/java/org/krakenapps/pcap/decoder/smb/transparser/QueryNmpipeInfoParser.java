package org.krakenapps.pcap.decoder.smb.transparser;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.transreq.QueryNmpipeInfoRequest;
import org.krakenapps.pcap.decoder.smb.transresp.QueryNmpipeInfoResponse;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class QueryNmpipeInfoParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer , Buffer parameterBuffer , Buffer dataBuffer) {
		QueryNmpipeInfoRequest transData = new QueryNmpipeInfoRequest();
		transData.setSubcommand(ByteOrderConverter.swap(setupBuffer.getShort()));
		transData.setFid(ByteOrderConverter.swap(setupBuffer.getShort()));
		transData.setLevel(ByteOrderConverter.swap(setupBuffer.getShort()));
		
		return transData;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer , Buffer paramterBuffer , Buffer dataBuffer , SmbSession session) {
		QueryNmpipeInfoResponse transData = new QueryNmpipeInfoResponse();
		transData.setOutputBufferSize(ByteOrderConverter.swap(setupBuffer.getShort()));
		transData.setInputBufferSize(ByteOrderConverter.swap(setupBuffer.getShort()));
		transData.setMaximumInstance(setupBuffer.get());
		transData.setCurrentInstance(setupBuffer.get());
		transData.setPipeNameLength(setupBuffer.get());
		transData.setPipeName(NetBiosNameCodec.readSmbUnicodeName(setupBuffer));
		return transData;
	}
	
	
}
