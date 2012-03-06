package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.ReadMPXSecondaryRequest;
import org.krakenapps.pcap.decoder.smb.response.ReadMPXSecondaryResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;

public class ReadMPXSecondaryParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h,Buffer b , SmbSession session) {
		SmbData data = new ReadMPXSecondaryRequest();
		//this packet has no request
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h,Buffer b ,SmbSession session) {
		SmbData data = new ReadMPXSecondaryResponse();
		//this packet has no response
		return data;
	}
// this is not use
}
