package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.SecurityPackageANDXRequest;
import org.krakenapps.pcap.decoder.smb.response.SecurityPackageANDXResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
// 0x7E
public class SecurityPackageANDXParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		SecurityPackageANDXRequest data = new SecurityPackageANDXRequest();
		
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		SecurityPackageANDXResponse data = new SecurityPackageANDXResponse();
		
		return data;
	}

	//retrun STATUS NOT IMPLEMENTED
}
