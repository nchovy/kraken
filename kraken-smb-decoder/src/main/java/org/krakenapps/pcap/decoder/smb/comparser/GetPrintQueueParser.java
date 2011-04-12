package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.GetPrintQueueRequest;
import org.krakenapps.pcap.decoder.smb.response.GetPrintQueueResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
//0xC3
public class GetPrintQueueParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		GetPrintQueueRequest data = new GetPrintQueueRequest();
		//not implement;
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		GetPrintQueueResponse data = new GetPrintQueueResponse();
		//not implement;
		return data;
	}

	// return STATUS_NOT_IMPLEMENTED;
}
