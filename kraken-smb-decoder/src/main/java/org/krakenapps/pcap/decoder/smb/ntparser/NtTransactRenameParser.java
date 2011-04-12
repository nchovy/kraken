package org.krakenapps.pcap.decoder.smb.ntparser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.ntreq.NtTransactRenameRequest;
import org.krakenapps.pcap.decoder.smb.ntresp.NtTransactRenameResponse;
import org.krakenapps.pcap.decoder.smb.transparser.TransParser;
import org.krakenapps.pcap.util.Buffer;

public class NtTransactRenameParser implements TransParser{

	@Override
	public TransData parseRequest(Buffer setupBuffer , Buffer parameterBuffer,  Buffer dataBuffer) {
		NtTransactRenameRequest transData = new NtTransactRenameRequest();
		return transData;
	}

	@Override
	public TransData parseResponse(Buffer setupBuffer , Buffer parameterBuffer,  Buffer dataBuffer , SmbSession session) {
		NtTransactRenameResponse transData = new NtTransactRenameResponse();
		return transData;
	}

}
