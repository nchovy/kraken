package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.WriteBulkDataRequest;
import org.krakenapps.pcap.decoder.smb.response.WriteBulkDataResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
//0xDA
public class WriteBulkDataParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		WriteBulkDataRequest data = new WriteBulkDataRequest();
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		WriteBulkDataResponse data = new WriteBulkDataResponse();
		// TODO Auto-generated method stub
		return data;
	}
// return STATUS_NOT_IMPLEMENTED
}
