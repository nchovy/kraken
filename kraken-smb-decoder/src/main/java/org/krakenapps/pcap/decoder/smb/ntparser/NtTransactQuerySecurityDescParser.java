package org.krakenapps.pcap.decoder.smb.ntparser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.ntreq.NtTransactQuerySecurityDescRequest;
import org.krakenapps.pcap.decoder.smb.ntresp.NtTransactQuerySecurityDescResponse;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class NtTransactQuerySecurityDescParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer , Buffer parameterBuffer,  Buffer dataBuffer) {
		NtTransactQuerySecurityDescRequest transData = new NtTransactQuerySecurityDescRequest();
		//there is no use of setup and data Buffer
		transData.setFid(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setReserved(ByteOrderConverter.swap(parameterBuffer.getShort()));
		transData.setSecurityInforFields(ByteOrderConverter.swap(parameterBuffer.getInt()));
		return transData;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer , Buffer parameterBuffer,  Buffer dataBuffer , SmbSession session) {
		NtTransactQuerySecurityDescResponse transData = new NtTransactQuerySecurityDescResponse();
		byte []securityDescriptor;
		//there is no use of setup Buffer
		transData.setLengthNeeded(ByteOrderConverter.swap(parameterBuffer.getInt()));
		securityDescriptor = new byte[dataBuffer.readableBytes()];
		parameterBuffer.gets(securityDescriptor);
		transData.setSecurityDescriptor(securityDescriptor);
		return transData;
	}
}
