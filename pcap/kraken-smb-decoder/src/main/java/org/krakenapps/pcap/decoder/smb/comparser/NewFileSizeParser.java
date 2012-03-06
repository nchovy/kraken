package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.NewFileSizeRequest;
import org.krakenapps.pcap.decoder.smb.response.NewFileSizeResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;

public class NewFileSizeParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		NewFileSizeRequest data = new NewFileSizeRequest();
		// not implement ERROR
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		NewFileSizeResponse data = new NewFileSizeResponse();
		// not implememt ERROR
		return data;
	}

	//// not implemented
	//  return STATUS_NOT_IMPLEMENTED
}
