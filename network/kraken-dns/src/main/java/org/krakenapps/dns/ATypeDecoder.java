/*
 * Copyright 2009 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.dns;

import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindori
 */
public class ATypeDecoder {
	private final Logger logger = LoggerFactory.getLogger(ATypeDecoder.class
			.getName());

	public DnsAnswer decodeBody_Answer(ByteBuffer buffer) {
		logger.info("Decoding Answer");
		DnsAnswer answer = new DnsAnswer();

		short queryName = buffer.getShort();
		answer.setQueryName(queryName);

		short queryType = buffer.getShort();
		answer.setQueryType(queryType);

		short queryClass = buffer.getShort();
		answer.setQueryClass(queryClass);

		byte timeToLive1 = buffer.get();
		byte timeToLive2 = buffer.get();
		byte timeToLive3 = buffer.get();
		byte timeToLive4 = buffer.get();
		DnsAddressParser addressParser = new DnsAddressParser();
		int timeToLive = addressParser.convertBytearrayToInt(timeToLive1,
				timeToLive2, timeToLive3, timeToLive4);
		answer.setTimeToLive(timeToLive);
		logger.info("Time to live : " + timeToLive + " seconds");

		short dataLength = buffer.getShort();
		answer.setDataLength(dataLength);
		logger.info("Data length : " + dataLength);

		int firstAddressByte = buffer.get() & 0xFF;
		int secondAddressByte = buffer.get() & 0xFF;
		int thirdAddressByte = buffer.get() & 0xFF;
		int fourthAddressByte = buffer.get() & 0xFF;
		String domainAddress = firstAddressByte + "." + secondAddressByte + "."
				+ thirdAddressByte + "." + fourthAddressByte;
		answer.setDomainAddress(domainAddress);
		logger.info("Domain Address : " + domainAddress);

		return answer;
	}

	public DnsAdditionalRecord decodeBody_AdditionalRecord(ByteBuffer buffer) {
		logger.info("Decoding Additional record");
		DnsAdditionalRecord record = new DnsAdditionalRecord();

		short queryName = buffer.getShort();
		record.setQueryName(queryName);

		short queryType = buffer.getShort();
		record.setQueryType(queryType);

		short queryClass = buffer.getShort();
		record.setQueryClass(queryClass);

		byte timeToLive1 = buffer.get();
		byte timeToLive2 = buffer.get();
		byte timeToLive3 = buffer.get();
		byte timeToLive4 = buffer.get();
		DnsAddressParser addressParser = new DnsAddressParser();
		int timeToLive = addressParser.convertBytearrayToInt(timeToLive1,
				timeToLive2, timeToLive3, timeToLive4);
		record.setTimeToLive(timeToLive);
		logger.info("Time to live : " + timeToLive + " seconds");

		short dataLength = buffer.getShort();
		record.setDataLength(dataLength);
		logger.info("Data length : " + dataLength);

		int firstAddressByte = buffer.get() & 0xFF;
		int secondAddressByte = buffer.get() & 0xFF;
		int thirdAddressByte = buffer.get() & 0xFF;
		int fourthAddressByte = buffer.get() & 0xFF;
		String domainAddress = firstAddressByte + "." + secondAddressByte + "."
				+ thirdAddressByte + "." + fourthAddressByte;
		record.setDomainAddress(domainAddress);
		logger.info("Domain Address : " + domainAddress);

		return record;
	}
}
