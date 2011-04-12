package org.krakenapps.dns;

import java.nio.ByteBuffer;

public class DnsAuthoritativeNameServerDecoder {
	
	public void decodeBody_AuthoritativeNameServers(
			DnsReply dnsReply, byte[] replyBuffer,
			ByteBuffer buffer, int numOfField) {
		if (numOfField == 0) {
			DnsAdditionalRecordDecoder recordDecoder = new DnsAdditionalRecordDecoder();
			recordDecoder.decodeBody_AdditionalRecords(dnsReply,
					replyBuffer, buffer, dnsReply.getHeader().getAdditionalRRs());
		} else {
			buffer.mark();
			buffer.getShort();
			short queryType = buffer.getShort();
			buffer.reset();

			DnsAuthoritativeNameServer authoritativeNameServer = new DnsAuthoritativeNameServer();
			switch (queryType) {
			case 0x02:
				NsTypeDecoder nsTypeDecoder = new NsTypeDecoder();
				authoritativeNameServer = nsTypeDecoder
						.decodeBody_AuthoritativeNameserver(replyBuffer, buffer);
				break;
			case 0x05:
				CnameTypeDecoder cnameTypeDecoder = new CnameTypeDecoder();
				authoritativeNameServer = cnameTypeDecoder
						.decodeBody_AuthoritativeNameserver(replyBuffer, buffer);
				break;
			case 0x06:
				SoaTypeDecoder soaTypeDecoder = new SoaTypeDecoder();
				authoritativeNameServer = soaTypeDecoder
						.decodeBody_AuthoritativeNameserver(replyBuffer, buffer);
				break;
			}

			dnsReply.setAuthoritativeNameServers(authoritativeNameServer);
			decodeBody_AuthoritativeNameServers(dnsReply, replyBuffer,
					buffer, numOfField - 1);
		}
	}
}
