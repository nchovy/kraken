package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.WriteMPXSecondaryRequest;
import org.krakenapps.pcap.decoder.smb.response.WriteMPXSecondaryResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;

public class WriteMPXSecondaryParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		WriteMPXSecondaryRequest data = new WriteMPXSecondaryRequest();
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		WriteMPXSecondaryResponse data = new WriteMPXSecondaryResponse();
		return data;
	}

	//not use
}
