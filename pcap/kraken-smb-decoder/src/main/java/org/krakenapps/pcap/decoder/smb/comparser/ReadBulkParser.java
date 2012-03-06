package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.ReadBulkRequest;
import org.krakenapps.pcap.decoder.smb.response.ReadBulkResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
//0xD8
public class ReadBulkParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		ReadBulkRequest data = new ReadBulkRequest();
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		ReadBulkResponse data = new ReadBulkResponse();
		return data;
	}

	//return STATUS_NOT_IMPLEMENTED;
}
