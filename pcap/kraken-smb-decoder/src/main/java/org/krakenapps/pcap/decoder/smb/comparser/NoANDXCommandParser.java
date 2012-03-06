package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.NoANDXCommandRequest;
import org.krakenapps.pcap.decoder.smb.response.NoANDXCommandResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
//0xFF
public class NoANDXCommandParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		NoANDXCommandRequest data = new NoANDXCommandRequest();
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		NoANDXCommandResponse data = new NoANDXCommandResponse();
		return data;
	}

	//return STATUS_SMB_BAD_COMMAD;
}
