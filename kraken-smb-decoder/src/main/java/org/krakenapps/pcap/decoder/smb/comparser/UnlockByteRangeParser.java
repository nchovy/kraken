package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.UnlockByteRangeRequest;
import org.krakenapps.pcap.decoder.smb.response.UnlockByteRangeResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class UnlockByteRangeParser implements SmbDataParser{
	
	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		UnlockByteRangeRequest data = new UnlockByteRangeRequest();
		data.setWordCount(b.get());
		if(data.getWordCount() == 0x05){
			data.setFid(ByteOrderConverter.swap(b.getShort()));
			data.setCountOfBytesToLock(ByteOrderConverter.swap(b.getInt()));
			data.setUnLockOffsetInBytes(ByteOrderConverter.swap(b.getInt()));
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		UnlockByteRangeResponse data = new UnlockByteRangeResponse();
		data.setWordCount(b.get());
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
}
