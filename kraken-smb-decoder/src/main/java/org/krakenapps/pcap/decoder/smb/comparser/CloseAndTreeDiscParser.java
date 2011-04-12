package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.CloseAndTreeDiscRequest;
import org.krakenapps.pcap.decoder.smb.response.CloseAndTreeDiscResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
// 0x31
public class CloseAndTreeDiscParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h ,Buffer b , SmbSession session) {
		CloseAndTreeDiscRequest data = new CloseAndTreeDiscRequest();
		// Not Implement ERROR
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h ,Buffer b, SmbSession session) {
		CloseAndTreeDiscResponse data = new CloseAndTreeDiscResponse();
		// Not Implement ERROR
		return data;
	}

	//no use
	// return STATUS_NOT_IMPLEMETED
}
