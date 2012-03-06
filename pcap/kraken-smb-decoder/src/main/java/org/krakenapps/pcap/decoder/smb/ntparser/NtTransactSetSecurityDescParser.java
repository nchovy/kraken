package org.krakenapps.pcap.decoder.smb.ntparser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.ntreq.NtTransactSetSecurityDescRequest;
import org.krakenapps.pcap.decoder.smb.ntresp.NtTransactSetSecurityDescResponse;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class NtTransactSetSecurityDescParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer , Buffer parameterBuffer,  Buffer dataBuffer) {
		NtTransactSetSecurityDescRequest transData = new NtTransactSetSecurityDescRequest();
		//there is no use setupBuffer
		//start parameterBuffer
		transData.setFid(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setReserved(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setSecurityinformation(ByteOrderConverter.swap(parameterBuffer.getInt()));
		//end of parameterBuffer
		//start DataBuffer
		byte []securityDescriptor = new byte[parameterBuffer.readableBytes()];
		parameterBuffer.gets(securityDescriptor);
		transData.setSecurityDescriptor(securityDescriptor);
		// end of dataBuffer
		return transData;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer , Buffer parameterBuffer,  Buffer dataBuffer , SmbSession session) {
		NtTransactSetSecurityDescResponse transData = new NtTransactSetSecurityDescResponse();
		// do not response
		return transData;
	}

}
