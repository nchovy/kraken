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
public class SoaTypeDecoder {
	private Logger logger = LoggerFactory.getLogger(SoaTypeDecoder.class);
//	private static final int RemainDataLength = 20;

	public DnsAuthoritativeNameServer decodeBody_AuthoritativeNameserver(
			byte[] replyBuffer, ByteBuffer buffer) {
		logger.info("Decoding Authoritative nameserver");

		DnsAuthoritativeNameServer authoritativeNameserver = new DnsAuthoritativeNameServer();

		short queryName = buffer.getShort();
		authoritativeNameserver.setQueryName(queryName);

		short queryType = buffer.getShort();
		authoritativeNameserver.setQueryType(queryType);
		logger.info("Answer Type : " + queryType);

		short queryClass = buffer.getShort();
		authoritativeNameserver.setQueryClass(queryClass);

		byte timeToLive1 = buffer.get();
		byte timeToLive2 = buffer.get();
		byte timeToLive3 = buffer.get();
		byte timeToLive4 = buffer.get();
		DnsAddressParser addressParser = new DnsAddressParser();
		int timeToLive = addressParser.convertBytearrayToInt(timeToLive1,
				timeToLive2, timeToLive3, timeToLive4);
		authoritativeNameserver.setTimeToLive(timeToLive);
		logger.info("Time to live : " + timeToLive + " seconds");

		short dataLength = buffer.getShort();
		authoritativeNameserver.setDataLength(dataLength);
		logger.info("Data length : " + dataLength);
		
		DnsNameParser dnsNameParser = new DnsNameParser();
		String primaryNameServer = dnsNameParser
				.parse(replyBuffer, buffer, dnsNameParser.calcDomainLength(buffer));
		logger.info("Primary name server : " + primaryNameServer);
		
		String responsibleMailBox = dnsNameParser
				.parse(replyBuffer, buffer, dnsNameParser.calcDomainLength(buffer));
		logger.info("Responsible authority's mailbox : " + responsibleMailBox);

		int serialNumber = buffer.getInt();
		authoritativeNameserver.setSerialNumber(serialNumber);
		logger.info("Serial number : " + serialNumber);

		int refreshInterval = buffer.getInt();
		authoritativeNameserver.setRefreshInterval(refreshInterval);
		logger.info("Refresh interval : " + refreshInterval);

		int retryInterval = buffer.getInt();
		authoritativeNameserver.setRetryInterval(retryInterval);
		logger.info("Retry interval : " + retryInterval);

		int expirationLimit = buffer.getInt();
		authoritativeNameserver.setExpirationLimit(expirationLimit);
		logger.info("Expiration limit : " + expirationLimit);

		int minimumTimeToLive = buffer.getInt();
		authoritativeNameserver.setMinimumTimeToLive(minimumTimeToLive);
		logger.info("Minimum TTL : " + minimumTimeToLive);

		return authoritativeNameserver;
	}
}
