package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.SessionSetupANDXExtendRequest;
import org.krakenapps.pcap.decoder.smb.request.SessionSetupANDXRequest;
import org.krakenapps.pcap.decoder.smb.response.SessionSetupANDXExtendResponse;
import org.krakenapps.pcap.decoder.smb.response.SessionSetupANDXResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SessionSetupANDXParser implements SmbDataParser{
	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		
		SmbData data;
		//SessionSetupANDXRequest data = new SessionSetupANDXRequest();
		byte []oemPassword;
		byte []unicodePassword;
		byte []pad;
		int count =0;
		if(session.getNegotiateResponseHeader().isFlag2ExtendedSecurity())
		{
			byte []securityBlob;
			
			data = new SessionSetupANDXExtendRequest();
			((SessionSetupANDXExtendRequest)data).setWordCount(b.get());
			if(((SessionSetupANDXExtendRequest)data).getWordCount() == 0x0C){
				((SessionSetupANDXExtendRequest)data).setAndxCommand(b.get());
				((SessionSetupANDXExtendRequest)data).setAndxReserved(b.get());
				((SessionSetupANDXExtendRequest)data).setAndxOffset(ByteOrderConverter.swap(b.getShort()));
				((SessionSetupANDXExtendRequest)data).setMaxBufferSize(ByteOrderConverter.swap(b.getShort()));
				((SessionSetupANDXExtendRequest)data).setMaxMpxCount(ByteOrderConverter.swap(b.getShort()));
				((SessionSetupANDXExtendRequest)data).setVcNumber(ByteOrderConverter.swap(b.getShort()));
				((SessionSetupANDXExtendRequest)data).setSessionKey(ByteOrderConverter.swap(b.getInt()));
				((SessionSetupANDXExtendRequest)data).setSecurityBlobLenth(ByteOrderConverter.swap(b.getShort()));
				((SessionSetupANDXExtendRequest)data).setReserved(ByteOrderConverter.swap(b.getInt()));
				((SessionSetupANDXExtendRequest)data).setCapabilities(ByteOrderConverter.swap(b.getInt()));
			}
			else{
				b.skip(((SessionSetupANDXExtendRequest)data).getWordCount()*2);	
				((SessionSetupANDXExtendRequest)data).setMalformed(true);
			}
			((SessionSetupANDXExtendRequest)data).setByteCount(ByteOrderConverter.swap(b.getShort()));
			if(b.readableBytes() != ((SessionSetupANDXExtendRequest)data).getByteCount()){
				data.setMalformed(true);
				return data;
			}
			else if(b.readableBytes() ==0){
				data.setMalformed(true);
				return data;
			}
			securityBlob = new byte[((SessionSetupANDXExtendRequest)data).getSecurityBlobLenth()];
			b.gets(securityBlob);
			((SessionSetupANDXExtendRequest)data).setSecurityBlob(securityBlob);
			if(h.isFlag2Unicode())
			{
				((SessionSetupANDXExtendRequest)data).setNativeOS(NetBiosNameCodec.readSmbUnicodeName(b));
				((SessionSetupANDXExtendRequest)data).setNativeLanMan(NetBiosNameCodec.readSmbUnicodeName(b));
			}
			else
			{
				((SessionSetupANDXExtendRequest)data).setNativeOS(NetBiosNameCodec.readOemName(b));
				((SessionSetupANDXExtendRequest)data).setNativeLanMan(NetBiosNameCodec.readOemName(b));
			}
			return data;
		}
		else
		{
			data = new SessionSetupANDXRequest();
		
			((SessionSetupANDXRequest)data).setWordCount(b.get());
			if(((SessionSetupANDXRequest)data).getWordCount() == 0x0D){
				((SessionSetupANDXRequest)data).setAndxCommand(b.get());
				((SessionSetupANDXRequest)data).setAndxReserved(b.get());
				((SessionSetupANDXRequest)data).setAndxOffset(ByteOrderConverter.swap(b.getShort()));
				((SessionSetupANDXRequest)data).setMaxBufferSize(ByteOrderConverter.swap(b.getShort()));
				((SessionSetupANDXRequest)data).setMaxMpxCount(ByteOrderConverter.swap(b.getShort()));
				((SessionSetupANDXRequest)data).setVcNumber(ByteOrderConverter.swap(b.getShort()));
				((SessionSetupANDXRequest)data).setSessionKey(ByteOrderConverter.swap(b.getInt()));
				((SessionSetupANDXRequest)data).setOemPasswordLen(ByteOrderConverter.swap(b.getShort()));
				((SessionSetupANDXRequest)data).setUnicodePasswordLen(ByteOrderConverter.swap(b.getShort()));
				((SessionSetupANDXRequest)data).setReserved(ByteOrderConverter.swap(b.getInt()));
				((SessionSetupANDXRequest)data).setCapabilities(ByteOrderConverter.swap(b.getInt()));
			}
			else{
				b.skip(((SessionSetupANDXRequest)data).getWordCount()*2);
				((SessionSetupANDXRequest)data).setMalformed(true);
			}
			((SessionSetupANDXRequest)data).setByteCount(ByteOrderConverter.swap(b.getShort()));
			if(b.readableBytes() !=((SessionSetupANDXRequest)data).getByteCount()){
				data.setMalformed(true);
				return data;
			}
			else if(b.readableBytes() ==0){
				data.setMalformed(true);
				return data;
			}
			oemPassword = new byte[((SessionSetupANDXRequest)data).getOemPasswordLen()];
			unicodePassword = new byte[((SessionSetupANDXRequest)data).getUnicodePasswordLen()];
	
	//		System.out.printf("unicodepassword = %d\n" , data.getUnicodePasswordLen());
			b.gets(oemPassword);
			b.gets(unicodePassword);
			((SessionSetupANDXRequest)data).setOemPassword(oemPassword);
			((SessionSetupANDXRequest)data).setUnicodePassword(unicodePassword);
			b.mark();
			for(int i=0; i<b.readableBytes();i++){
				if(b.get() != (0x00)){
					break;
				}
				else{
					count++;
				}
			}
			b.rewind();
			pad = new byte[count];
			b.gets(pad);
			((SessionSetupANDXRequest)data).setPad(pad);
			if(h.isFlag2Unicode())
			{
				((SessionSetupANDXRequest)data).setAccountName(NetBiosNameCodec.readSmbUnicodeName(b));
				((SessionSetupANDXRequest)data).setNativeOS(NetBiosNameCodec.readSmbUnicodeName(b));
				((SessionSetupANDXRequest)data).setNativeLanMan(NetBiosNameCodec.readSmbUnicodeName(b));
			}
			else
			{
				((SessionSetupANDXRequest)data).setAccountName(NetBiosNameCodec.readOemName(b));
				((SessionSetupANDXRequest)data).setNativeOS(NetBiosNameCodec.readOemName(b));
				((SessionSetupANDXRequest)data).setNativeLanMan(NetBiosNameCodec.readOemName(b));
			}
		}
			return data;
	}
	
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		SmbData data;
		int count=0;
		byte []pad;
		if(session.getNegotiateRequestHeader().isFlag2ExtendedSecurity())
		{
			byte []securityBlob;
			data = new SessionSetupANDXExtendResponse();
			((SessionSetupANDXExtendResponse)data).setWordCount(b.get());
			if(((SessionSetupANDXExtendResponse)data).getWordCount() == 0x04){
				((SessionSetupANDXExtendResponse)data).setAndxCommand(b.get());
				((SessionSetupANDXExtendResponse)data).setAndxResrved(b.get());
				((SessionSetupANDXExtendResponse)data).setAndxoffset(ByteOrderConverter.swap(b.getShort()));
				((SessionSetupANDXExtendResponse)data).setAction(ByteOrderConverter.swap(b.getShort()));
				((SessionSetupANDXExtendResponse)data).setSecurityBlobLenth(ByteOrderConverter.swap(b.getShort()));
			}
			else{
				b.skip(((SessionSetupANDXExtendResponse)data).getWordCount()*2);
				((SessionSetupANDXExtendResponse)data).setMalformed(true);
			}
			((SessionSetupANDXExtendResponse)data).setByteCount(ByteOrderConverter.swap(b.getShort()));
			if(b.readableBytes() != ((SessionSetupANDXExtendResponse)data).getByteCount()){
				data.setMalformed(true);
				return data;
			}
			else if( b.readableBytes()==0){
				data.setMalformed(true);
				return data;
			}
			securityBlob = new byte[((SessionSetupANDXExtendResponse)data).getSecurityBlobLenth()];
			b.gets(securityBlob);
			((SessionSetupANDXExtendResponse)data).setSecurityBlob(securityBlob);
			if(h.isFlag2Unicode())
			{
				((SessionSetupANDXExtendResponse)data).setNativeOS(NetBiosNameCodec.readSmbUnicodeName(b));
				((SessionSetupANDXExtendResponse)data).setNativeLanMan(NetBiosNameCodec.readSmbUnicodeName(b));
			}
			else
			{
				((SessionSetupANDXExtendResponse)data).setNativeOS(NetBiosNameCodec.readOemName(b));
				((SessionSetupANDXExtendResponse)data).setNativeLanMan(NetBiosNameCodec.readOemName(b));
			}
			
		}
		else
		{
			data = new SessionSetupANDXResponse();
			((SessionSetupANDXResponse)data).setWordCount(b.get());
			if(((SessionSetupANDXResponse)data).getWordCount() == 0x03){
				((SessionSetupANDXResponse)data).setAndxCommand(b.get());
				((SessionSetupANDXResponse)data).setAndxResrved(b.get());
				((SessionSetupANDXResponse)data).setAndxoffset(ByteOrderConverter.swap(b.getShort()));
				((SessionSetupANDXResponse)data).setAction(ByteOrderConverter.swap(b.getShort()));
			}
			else{
				b.skip(((SessionSetupANDXResponse)data).getWordCount()*2);
				((SessionSetupANDXResponse)data).setMalformed(true);
			}
			((SessionSetupANDXResponse)data).setByteCount(ByteOrderConverter.swap(b.getShort()));
			if(b.readableBytes() != ((SessionSetupANDXResponse)data).getByteCount()){
				data.setMalformed(true);
				return data;
			}
			b.mark();
			for(int i=0; i<b.readableBytes();i++){
				if(b.get() != (0x00)){
					break;
				}
				else{
					count++;
				}
			}
			b.rewind();
			pad = new byte[count];
			b.gets(pad);
			((SessionSetupANDXResponse)data).setPad(pad);
			if(h.isFlag2Unicode())
			{
				((SessionSetupANDXResponse)data).setNativeOS(NetBiosNameCodec.readSmbUnicodeName(b));
				((SessionSetupANDXResponse)data).setNativeLanMan(NetBiosNameCodec.readSmbUnicodeName(b));
				((SessionSetupANDXResponse)data).setPrimaryDomain(NetBiosNameCodec.readSmbUnicodeName(b));
			}
			else
			{
				((SessionSetupANDXResponse)data).setNativeOS(NetBiosNameCodec.readOemName(b));
				((SessionSetupANDXResponse)data).setNativeLanMan(NetBiosNameCodec.readOemName(b));
				((SessionSetupANDXResponse)data).setPrimaryDomain(NetBiosNameCodec.readOemName(b));
			}
		}
		return data;
	}
}
