package org.krakenapps.dns;

import java.nio.ByteBuffer;

public class DnsAnswerDecoder {
		
	public void decodeBody_Answers(DnsReply dnsReply,
			byte[] replyBuffer, ByteBuffer buffer, int numOfField) {
		if (numOfField == 0) {
			DnsAuthoritativeNameServerDecoder authoritativeNameServerDecoder = new DnsAuthoritativeNameServerDecoder();
			authoritativeNameServerDecoder.decodeBody_AuthoritativeNameServers(
					dnsReply, replyBuffer, buffer, dnsReply.getHeader().getAuthorityRRs());
		} else {
			buffer.mark();
			buffer.getShort();
			short queryType = buffer.getShort();
			buffer.reset();

			DnsAnswer answer = new DnsAnswer();
			switch (queryType) {
			case 0x01:
				ATypeDecoder aTypeDecoder = new ATypeDecoder();
				answer = aTypeDecoder.decodeBody_Answer(buffer);
				break;
			case 0x02:
				NsTypeDecoder nsTypeDecoder = new NsTypeDecoder();
				answer = nsTypeDecoder.decodeBody_Answer(replyBuffer, buffer);
				break;
			case 0x05:
				CnameTypeDecoder cnameTypeDecoder = new CnameTypeDecoder();
				answer = cnameTypeDecoder
						.decodeBody_Answer(replyBuffer, buffer);
				break;
			case 0x0F:
				MxTypeDecoder mxTypeDecoder = new MxTypeDecoder();
				answer = mxTypeDecoder.decodeBody_Answer(replyBuffer, buffer);
				break;
			}

			dnsReply.setAnswers(answer);
			decodeBody_Answers(dnsReply, replyBuffer, buffer,
					numOfField - 1);
		}
	}
}