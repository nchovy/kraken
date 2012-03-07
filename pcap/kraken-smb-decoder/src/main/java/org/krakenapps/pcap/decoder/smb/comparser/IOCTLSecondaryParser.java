package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.IOCTLSecondaryRequest;
import org.krakenapps.pcap.decoder.smb.response.IOCTLSecondaryResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;

public class IOCTLSecondaryParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		IOCTLSecondaryRequest data = new IOCTLSecondaryRequest();
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		IOCTLSecondaryResponse data = new IOCTLSecondaryResponse();
		return data;
	}
	// reserved;
	// return Status NOT Implemented
}
