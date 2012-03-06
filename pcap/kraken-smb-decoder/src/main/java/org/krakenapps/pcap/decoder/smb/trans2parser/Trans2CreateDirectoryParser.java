package org.krakenapps.pcap.decoder.smb.trans2parser;

import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.trans2req.Trans2CreateDirectoryRequest;
import org.krakenapps.pcap.decoder.smb.trans2resp.Trans2CreateDirectoryResponse;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class Trans2CreateDirectoryParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer , Buffer parameterBuffer , Buffer dataBuffer) {
		Trans2CreateDirectoryRequest transData = new Trans2CreateDirectoryRequest();
		transData.setSubcommand(ByteOrderConverter.swap(setupBuffer.getShort()));
		transData.setReserved(ByteOrderConverter.swap(parameterBuffer.getInt()));
		transData.setDirectoryName(NetBiosNameCodec.readSmbUnicodeName(parameterBuffer));
		// TODO read smb fea list
		return transData;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer , Buffer parameterBuffer , Buffer dataBuffer , SmbSession session) {
		Trans2CreateDirectoryResponse transData = new Trans2CreateDirectoryResponse();
		transData.setEaErrorOffset(ByteOrderConverter.swap(parameterBuffer.getShort()));
		return transData;
	}

}
