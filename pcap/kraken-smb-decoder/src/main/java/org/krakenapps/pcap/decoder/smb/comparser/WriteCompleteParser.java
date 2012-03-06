package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.WriteCompleteRequest;
import org.krakenapps.pcap.decoder.smb.response.WriteCompleteResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
//0x20
import org.krakenapps.pcap.util.Buffer;
public class WriteCompleteParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		SmbData data = new WriteCompleteRequest(); 
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		SmbData data = new WriteCompleteResponse();
		return data;
	}
 //SmbComWriteRaw final response
}
