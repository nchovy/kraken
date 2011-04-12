package org.krakenapps.pcap.decoder.smb.comparser;

import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.SetInfo2Request;
import org.krakenapps.pcap.decoder.smb.response.SetInfo2Response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

//0x22
public class SetInfo2Parser implements SmbDataParser {
	@Override
	public SmbData parseRequest(SmbHeader h, Buffer b, SmbSession session) {
		SetInfo2Request data = new SetInfo2Request();
		data.setWordCount(b.get());
		data.setFid(ByteOrderConverter.swap(b.getShort()));
		data.setCreateDate(ByteOrderConverter.swap(b.getShort()));
		data.setCreationTime(ByteOrderConverter.swap(b.getShort()));
		data.setLastAccessDate(ByteOrderConverter.swap(b.getShort()));
		data.setLastAccessTime(ByteOrderConverter.swap(b.getShort()));
		data.setLastWriteDate(ByteOrderConverter.swap(b.getShort()));
		data.setLastWriteTime(ByteOrderConverter.swap(b.getShort()));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}

	@Override
	public SmbData parseResponse(SmbHeader h, Buffer b, SmbSession session) {
		SetInfo2Response data = new SetInfo2Response();
		data.setWordCount(b.get());
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
}
