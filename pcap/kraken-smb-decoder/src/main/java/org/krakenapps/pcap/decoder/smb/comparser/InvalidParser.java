package org.krakenapps.pcap.decoder.smb.comparser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.InvalidRequest;
import org.krakenapps.pcap.decoder.smb.response.InvalidResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;

public class InvalidParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h, Buffer b , SmbSession session) {
		InvalidRequest data = new InvalidRequest();
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h, Buffer b ,SmbSession session) {
		InvalidResponse data = new InvalidResponse();
		return data;
	}

}
