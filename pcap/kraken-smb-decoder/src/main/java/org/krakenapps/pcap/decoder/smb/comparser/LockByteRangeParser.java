package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.LockByteRangeRequest;
import org.krakenapps.pcap.decoder.smb.response.LockByteRangeResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
// 0x0c
public class LockByteRangeParser implements SmbDataParser{
	@Override
	public SmbData parseRequest(SmbHeader h,Buffer b , SmbSession session) {
		LockByteRangeRequest data = new LockByteRangeRequest();
		data.setWordCount(b.get());
		data.setFid(ByteOrderConverter.swap(b.getShort()));
		data.setCountOfBytesToLock(ByteOrderConverter.swap(b.getInt()));
		data.setLockOffsetInBytes(ByteOrderConverter.swap(b.getInt()));
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		LockByteRangeResponse data = new LockByteRangeResponse();
		data.setWordCount(b.get());
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
}
