package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.TreeConnectANDXRequest;
import org.krakenapps.pcap.decoder.smb.response.TreeConnectANDXExtendResponse;
import org.krakenapps.pcap.decoder.smb.response.TreeConnectANDXResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class TreeConnectANDXParser implements SmbDataParser{
	
	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		TreeConnectANDXRequest data = new TreeConnectANDXRequest();
		byte []password;
		byte []pad;
		int count=0;
		data.setWordCount(b.get());
		if(data.getWordCount() == 0x04){
			data.setAndxCommand(b.get());
			data.setAndxReserved(b.get());
			data.setAndxOffset(ByteOrderConverter.swap(b.getShort()));
			data.setFlags(ByteOrderConverter.swap(b.getShort()));
			data.setPasswordLength(ByteOrderConverter.swap(b.getShort()));
		}
		else{
			data.setMalformed(true);
			b.skip(data.getWordCount()*2);
		}
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		password = new byte[data.getPasswordLength()];
		b.gets(password);
		data.setPassword(password);
		b.mark();
		for(int i=0; i<b.readableBytes();i++){
			if(b.get() != (0x00)){
				break;
			}
			else{
				count++;
			}
		}
		b.reset();
		pad = new byte[count];
		b.gets(pad);
		data.setPad(pad);
		if(h.isFlag2Unicode()){
			data.setPath(NetBiosNameCodec.readSmbUnicodeName(b));
		}
		else{
			data.setPath(NetBiosNameCodec.readOemName(b));
		}
		if(h.isFlag2Unicode()){
			data.setService(NetBiosNameCodec.readSmbUnicodeName(b));
		}
		else{
			data.setService(NetBiosNameCodec.readOemName(b));
		}
		
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		SmbData data;
		if( ((TreeConnectANDXRequest)(session.getUseSessionData())).isTreeConnectAndxExtendedResponse())
		{
			data = new TreeConnectANDXExtendResponse();
			((TreeConnectANDXExtendResponse)(data)).setWordCount(b.get());
			if(((TreeConnectANDXExtendResponse)(data)).getWordCount() == 0x07){
				((TreeConnectANDXExtendResponse)(data)).setAndxCommand(b.get());
				((TreeConnectANDXExtendResponse)(data)).setAndxReserved(b.get());
				((TreeConnectANDXExtendResponse)(data)).setAndxOffset(ByteOrderConverter.swap(b.getShort()));
				((TreeConnectANDXExtendResponse)(data)).setOptionalSupport(ByteOrderConverter.swap(b.getShort()));
				((TreeConnectANDXExtendResponse)(data)).setMaximalShareAccessRight(ByteOrderConverter.swap(b.getInt()));
				((TreeConnectANDXExtendResponse)(data)).setGuestMaximalShareAccessRight(ByteOrderConverter.swap(b.getInt()));
			}	
			else{
				((TreeConnectANDXExtendResponse)(data)).setMalformed(true);
				b.skip(((TreeConnectANDXExtendResponse)(data)).getWordCount()*2);
			}
			((TreeConnectANDXExtendResponse)(data)).setByteCount(ByteOrderConverter.swap(b.getShort()));
			if(b.readableBytes() != ((TreeConnectANDXExtendResponse)data).getByteCount()){
				data.setMalformed(true);
				return data;
			}
			((TreeConnectANDXExtendResponse)(data)).setService(NetBiosNameCodec.readOemName(b));
			((TreeConnectANDXExtendResponse)(data)).setNativeFileSystem(NetBiosNameCodec.readOemName(b));
		}
		else
		{
			data = new TreeConnectANDXResponse();
			byte []pad;
			int count=0;
			((TreeConnectANDXResponse)data).setWordCount(b.get());
			if(((TreeConnectANDXResponse)data).getWordCount() ==0x03){
				((TreeConnectANDXResponse)data).setAndxCommand(b.get());
				((TreeConnectANDXResponse)data).setAndxReserved(b.get());
				((TreeConnectANDXResponse)data).setAndxOffset(ByteOrderConverter.swap(b.getShort()));
				((TreeConnectANDXResponse)data).setOptionalSupport(ByteOrderConverter.swap(b.getShort()));
			}
			else{
				((TreeConnectANDXResponse)data).setMalformed(true);
				b.skip(((TreeConnectANDXResponse)data).getWordCount()*2);
			}
			((TreeConnectANDXResponse)data).setByteCount(ByteOrderConverter.swap(b.getShort()));
			if(b.readableBytes() != ((TreeConnectANDXResponse)data).getByteCount()){
				data.setMalformed(true);
				return data;
			}
			else if(b.readableBytes() ==0){
				data.setMalformed(true);
				return data;
			}
		//	System.out.printf("byte count = %x\n" , data.getByteCount());
			((TreeConnectANDXResponse)data).setService(NetBiosNameCodec.readOemName(b));
		//	System.out.printf("password length = %s\n" , data.getService());
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
			((TreeConnectANDXResponse)data).setPad(pad);
			((TreeConnectANDXResponse)data).setNativeFileSystem(NetBiosNameCodec.readOemName(b));
		}
		return data;
	}

}
