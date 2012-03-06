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

/**
 * @author mindori
 */
public class DnsQueryEncoder {
//	private static final int DNS_HEADER_LENGTH = 12;

	public static byte[] encode(DnsHeader header, DnsQuery query) {
		ByteBuffer dnsBuffer = ByteBuffer.allocate(1000);
		dnsBuffer.putShort((short) 2);
		dnsBuffer.putShort(encodeFlags(header.getFlags()));
		dnsBuffer.putShort(header.getQuestions());
		dnsBuffer.putShort(header.getAnswerRRs());
		dnsBuffer.putShort(header.getAuthorityRRs());
		dnsBuffer.putShort(header.getAdditionalRRs());
		dnsBuffer.put(dnsNameToBytes(query.getDomainName()));
		dnsBuffer.put((byte) 0); // end of domain name
		dnsBuffer.putShort(query.getQueryType());
		dnsBuffer.putShort(query.getQueryClass());
		dnsBuffer.flip();
		byte[] buffer = new byte[dnsBuffer.remaining()];
		dnsBuffer.get(buffer, 0, dnsBuffer.limit());
		return buffer;
	}

	public static int calcDomainLength(String dnsName) {
		byte[] buffer = dnsNameToBytes(dnsName);
		return buffer.length;
	}

	private static byte[] dnsNameToBytes(String dnsName) {
		byte[] buffer = new byte[dnsName.length() + 1];
		String[] tokens = dnsName.split("\\.");

		int countPosition = 0;
		for (String token : tokens) {
			buffer[countPosition] = (byte) token.length();

			for (int i = 0; i < token.length(); ++i) {
				buffer[countPosition + i + 1] = (byte) token.charAt(i);
			}

			countPosition += token.length() + 1;
		}

		return buffer;
	}

	private static short encodeFlags(DnsFlags flags) {
		short s = 0;
		s |= booleanToBit(flags.isQuery()) << 15;
		s |= flags.getOpCode() << 11;
		s |= booleanToBit(flags.isAuthoritativeAnswer()) << 10;
		s |= booleanToBit(flags.isTruncated()) << 9;
		s |= booleanToBit(flags.isRecursionDesired()) << 8;
		s |= booleanToBit(flags.isRecursionAvailable()) << 7;
		s |= booleanToBit(flags.isAnswerAuthenticated()) << 5;
		s |= booleanToBit(flags.isNonAuthenticatedDataOK()) << 4;
		s |= flags.getReplyCode();
		return s;
	}

	private static byte booleanToBit(boolean b) {
		return b == true ? (byte) 1 : (byte) 0;
	}
}
