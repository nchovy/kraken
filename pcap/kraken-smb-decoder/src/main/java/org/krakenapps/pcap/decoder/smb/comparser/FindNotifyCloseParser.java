package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.FindNotifyCloseRequest;
import org.krakenapps.pcap.decoder.smb.response.FindNotifyCloseResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
//0x30
public class FindNotifyCloseParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		FindNotifyCloseRequest data = new FindNotifyCloseRequest();
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		FindNotifyCloseResponse data = new FindNotifyCloseResponse();
		return data;
	}

	// this code have no use
	// if receive this code , must return STATUS_NOT_IMPLEMETED 
}
