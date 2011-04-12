package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.LockingANDXRequest;
import org.krakenapps.pcap.decoder.smb.response.LockingANDXResponse;
import org.krakenapps.pcap.decoder.smb.structure.LockingANDXRange32;
import org.krakenapps.pcap.decoder.smb.structure.LockingANDXRange64;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
//0x24
public class LockingANDXParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		LockingANDXRequest data = new LockingANDXRequest();
		data.setWordCount(b.get());
		if(data.getWordCount() == 0x08){
			data.setAndxCommand(b.get());
			data.setAndxReserved(b.get());
			data.setAndxOffset(ByteOrderConverter.swap(b.getShort()));
			data.setFid(ByteOrderConverter.swap(b.getShort()));
			data.setTypeOfLock(b.get());
			data.setNewOpLockLevel(b.get());
			data.setTimeout(b.getInt());
			data.setNumberOfreqedUnlocks(ByteOrderConverter.swap(b.getShort()));
			data.setNumberOfreqedLocks(ByteOrderConverter.swap(b.getShort()));
		}
		else
		{
			data.setMalformed(true);
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		if( data.getByteCount() == 
			( data.getNumberOfreqedLocks()*10) +
			( data.getNumberOfreqedUnlocks()*10	)){
			LockingANDXRange32 []locks;  
			LockingANDXRange32 []unlocks;
			locks = new LockingANDXRange32[data.getNumberOfreqedLocks()];
			for(int i=0;i<data.getNumberOfreqedLocks();i++)
			{
				locks[i] = new LockingANDXRange32();
				locks[i].parse(b);
			}
			data.setLocks32(locks);
			unlocks = new LockingANDXRange32[data.getNumberOfreqedUnlocks()];
			for(int i=0;i<data.getNumberOfreqedUnlocks();i++)
			{
				unlocks[i] = new LockingANDXRange32();
				unlocks[i].parse(b);
			}
			data.setUnlocks32(unlocks);
		}
		else{
			LockingANDXRange64 []locks;  
			LockingANDXRange64 []unlocks;
			locks = new LockingANDXRange64[data.getNumberOfreqedLocks()];
			unlocks = new LockingANDXRange64[data.getNumberOfreqedUnlocks()];
			for(int i=0;i<data.getNumberOfreqedLocks();i++)
			{
				locks[i] = new LockingANDXRange64();
				locks[i].parse(b);
			}
			data.setLocks64(locks);
			unlocks = new LockingANDXRange64[data.getNumberOfreqedUnlocks()];
			for(int i=0;i<data.getNumberOfreqedUnlocks();i++)
			{
				unlocks[i] = new LockingANDXRange64();
				unlocks[i].parse(b);
			}
			data.setUnlocks64(unlocks);
		}
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		LockingANDXResponse data = new LockingANDXResponse();
		data.setWordCount(b.get());
		if(data.getWordCount() ==0x02){
			data.setAndxCommand(b.get());
			data.setAndxReserved(b.get());
			data.setAndxOffset(b.get());
		}
		else
		{
			data.setMalformed(true);
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		return data;
	}
}
