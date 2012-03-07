package org.krakenapps.pcap.decoder.smb.comparser;
import org.krakenapps.pcap.decoder.netbios.NetBiosNameCodec;
import org.krakenapps.pcap.decoder.smb.SmbSession;
import org.krakenapps.pcap.decoder.smb.request.NegotiateRequest;
import org.krakenapps.pcap.decoder.smb.response.NegotiateResponse;
import org.krakenapps.pcap.decoder.smb.response.NegotiateSecurityExtendResponse;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.decoder.smb.structure.SmbDialect;
import org.krakenapps.pcap.decoder.smb.structure.SmbHeader;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class NegotiateParser implements SmbDataParser{

	@Override
	public SmbData parseRequest(SmbHeader h , Buffer b , SmbSession session) {
		NegotiateRequest data = new NegotiateRequest();
		SmbDialect []dialects;
		int count=0;
		data.setWordCount(b.get());
		data.setByteCount(ByteOrderConverter.swap(b.getShort()));
		if(b.readableBytes() != data.getByteCount()){
			data.setMalformed(true);
			return data;
		}
		//dialects.setBufferFormat(b.get());
		
		b.mark();
		for(int i=0; i<data.getByteCount(); i++)
		{
			if(b.get() == 0x00)
			{
				count++;
			}
		}
		b.reset();
		dialects = new SmbDialect[count];
		for(int i=0;i<count;i++){
			dialects[i] = new SmbDialect();
			dialects[i].setBufferFormat(b.get());
			if(h.isFlag2Unicode()){
				dialects[i].setDialectString(NetBiosNameCodec.readSmbUnicodeName(b));
			}
			else{
				dialects[i].setDialectString(NetBiosNameCodec.readOemName(b));
			}
		}
		data.setDialects(dialects);
		return data;
	}
	@Override
	public SmbData parseResponse(SmbHeader h , Buffer b ,SmbSession session) {
		SmbData data;
		if(session.getUseSessionHeader().isFlag2ExtendedSecurity())
		{
			data = new NegotiateSecurityExtendResponse();
			byte []serverGUID = new byte[16];
			byte []securityBlob;
			((NegotiateSecurityExtendResponse)data).setWordCount(b.get());
			((NegotiateSecurityExtendResponse)data).setDialectIndex(ByteOrderConverter.swap(b.getShort()));
			((NegotiateSecurityExtendResponse)data).setSercurityMode(b.get());
			((NegotiateSecurityExtendResponse)data).setMaxMpxCount(ByteOrderConverter.swap(b.getShort()));
			((NegotiateSecurityExtendResponse)data).setMaxNumberVcs(ByteOrderConverter.swap(b.getShort()));
			((NegotiateSecurityExtendResponse)data).setMaxBufferSize(ByteOrderConverter.swap(b.getInt()));
			((NegotiateSecurityExtendResponse)data).setMaxRawSize(ByteOrderConverter.swap(b.getInt()));
			((NegotiateSecurityExtendResponse)data).setSessionKey(ByteOrderConverter.swap(b.getInt()));
			((NegotiateSecurityExtendResponse)data).setCapabilities(ByteOrderConverter.swap(b.getInt()));
			((NegotiateSecurityExtendResponse)data).setSystemTime(ByteOrderConverter.swap(b.getLong()));
			((NegotiateSecurityExtendResponse)data).setServerTimeZone(ByteOrderConverter.swap(b.getShort()));
			((NegotiateSecurityExtendResponse)data).setChallengeLength(b.get());
			//TODO : extended
			((NegotiateSecurityExtendResponse)data).setByteCount(ByteOrderConverter.swap(b.getShort()));
			b.gets(serverGUID);
			((NegotiateSecurityExtendResponse)data).setServerGUID(serverGUID);
	//		System.out.println(((NegotiateSecurityExtendResponse)data).getByteCount());
			securityBlob = new byte[((NegotiateSecurityExtendResponse)data).getByteCount()-16];
			b.gets(securityBlob);
			((NegotiateSecurityExtendResponse)data).setSecurityBlob(securityBlob);
		}
		else
		{
			data = new NegotiateResponse();
			byte []challenge;
			((NegotiateResponse)data).setWordCount(b.get());
			if(((NegotiateResponse)data).getWordCount() == 0x00){
				((NegotiateResponse)data).setByteCount(ByteOrderConverter.swap(b.getShort()));
			}
			else if(((NegotiateResponse)data).getWordCount() == 0x01){ // core Protocol
				((NegotiateResponse)data).setDialectIndex(ByteOrderConverter.swap(b.getShort()));
				((NegotiateResponse)data).setByteCount(ByteOrderConverter.swap(b.getShort()));
			//data.set
			}
			else if(((NegotiateResponse)data).getWordCount() == 0x11){ // NT LAN Manger 
				((NegotiateResponse)data).setDialectIndex(ByteOrderConverter.swap(b.getShort()));
				((NegotiateResponse)data).setSercurityMode(b.get());
				((NegotiateResponse)data).setMaxMpxCount(ByteOrderConverter.swap(b.getShort()));
				((NegotiateResponse)data).setMaxNumberVcs(ByteOrderConverter.swap(b.getShort()));
				((NegotiateResponse)data).setMaxBufferSize(ByteOrderConverter.swap(b.getInt()));
				((NegotiateResponse)data).setMaxRawSize(ByteOrderConverter.swap(b.getInt()));
				((NegotiateResponse)data).setSessionKey(ByteOrderConverter.swap(b.getInt()));
				((NegotiateResponse)data).setCapabilities(ByteOrderConverter.swap(b.getInt()));
				((NegotiateResponse)data).setSystemTime(b.getLong());
				((NegotiateResponse)data).setServerTimeZone(ByteOrderConverter.swap(b.getShort()));
				((NegotiateResponse)data).setChallengeLenghth(b.get());
				((NegotiateResponse)data).setByteCount(ByteOrderConverter.swap(b.getShort()));
				if(b.readableBytes() != ((NegotiateResponse)data).getByteCount()){
					data.setMalformed(true);
					return data;
				}
				challenge = new byte[((NegotiateResponse)data).getChallengeLenghth()];
				b.gets(challenge);
				((NegotiateResponse)data).setChallenge(challenge);
				if(h.isFlag2Unicode()){
					((NegotiateResponse)data).setDomainName(NetBiosNameCodec.readSmbUnicodeName(b));
				}
				else{
					((NegotiateResponse)data).setDomainName(NetBiosNameCodec.readOemName(b));
				}
			}
			else if(((NegotiateResponse)data).getWordCount() == 0x0D){ // LanManger 1.0 throgh 2.1
				
			}
			//TODO CIFS 313Page not specify this
		}
		return data;
	}
	
}
