package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.QueryInfoDiskRequest;
import org.krakenapps.pcap.decoder.smb.response.QueryInfoDiskResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
//0x80
public class QueryInfoDiskParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		QueryInfoDiskRequest data = new QueryInfoDiskRequest();
		data.setWordCount(b.get());
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		QueryInfoDiskResponse data = new QueryInfoDiskResponse();
		data.setWordCount(b.get());
		data.setTotalUnits(ByteOrderConverter.swap(b.getShort()));
		data.setBlocksPerUnit(ByteOrderConverter.swap(b.getShort()));
		data.setBlockSize(ByteOrderConverter.swap(b.getShort()));
		data.setFreeUnits(ByteOrderConverter.swap(b.getShort()));
		data.setReserved(ByteOrderConverter.swap(b.getShort()));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
}
