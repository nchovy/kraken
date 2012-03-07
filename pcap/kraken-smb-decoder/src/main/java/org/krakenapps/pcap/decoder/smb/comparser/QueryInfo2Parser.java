package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.QueryInfo2Request;
import org.krakenapps.pcap.decoder.smb.response.QueryInfo2Response;
import org.krakenapps.pcap.decoder.smb.rr.FileAttributes;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
//0x23
public class QueryInfo2Parser implements SmbDataParser{
	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		QueryInfo2Request data = new QueryInfo2Request();
		data.setWordCount(b.get());
		data.setFid(ByteOrderConverter.swap(b.getShort()));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		QueryInfo2Response data = new QueryInfo2Response();
		data.setWordCount(b.get());
		data.setCreateDate(ByteOrderConverter.swap(b.getShort()));
		data.setCreateTime(ByteOrderConverter.swap(b.getShort()));
		data.setLastAccessDate(ByteOrderConverter.swap(b.getShort()));
		data.setLastAccessTime(ByteOrderConverter.swap(b.getShort()));
		data.setLastWriteDate(ByteOrderConverter.swap(b.getShort()));
		data.setLastWriteTime(ByteOrderConverter.swap(b.getShort()));
		data.setFileDateSize(b.getInt());
		data.setFileAllocationSize(b.getInt());
		data.setFileAttributes(FileAttributes.parse(b.get() & 0xff));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
}
