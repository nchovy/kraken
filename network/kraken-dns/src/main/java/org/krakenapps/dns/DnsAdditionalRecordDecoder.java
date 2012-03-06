package org.krakenapps.dns;

import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnsAdditionalRecordDecoder {
	private Logger logger = LoggerFactory
			.getLogger(DnsAdditionalRecordDecoder.class);

	public void decodeBody_AdditionalRecords(DnsReply dnsReply,
			byte[] replyBuffer, ByteBuffer buffer, int numOfField) {
		if (numOfField == 0) {
			logger.info("Packet receive complete.");
		} else {
			buffer.mark();	
			buffer.getShort();
			short queryType = buffer.getShort();
			buffer.reset();

			DnsAdditionalRecord record = new DnsAdditionalRecord();
			switch (queryType) {
			case 0x01:
				ATypeDecoder aTypeDecoder = new ATypeDecoder();
				record = aTypeDecoder.decodeBody_AdditionalRecord(buffer);
				break;
			}

			dnsReply.setAdditionalRecords(record);
			decodeBody_AdditionalRecords(dnsReply, replyBuffer, buffer,
					numOfField - 1);
		}
	}
}
