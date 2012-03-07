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

import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindori
 */
public class DnsReplyDecoder {
	private Logger logger = LoggerFactory.getLogger(DnsReplyDecoder.class);

	public void decodeHeader(ByteBuffer buffer, DnsReply dnsReply) {
		try {
			byte[] replyBuffer = buffer.array();

			logger.info("Decoding Header");
			DnsHeader header = new DnsHeader();

			short transactionId = buffer.getShort();
			header.setTransactionId(transactionId);
			logger.info("Transaction ID : " + transactionId);

			short flags = buffer.getShort();
			header.setFlags(new DnsFlags(flags));
			if (header.getFlags().getReplyCode() == 3) {
				throw new UnknownHostException();
			}

			short questions = buffer.getShort();
			header.setQuestions(questions);
			logger.info("Questions : " + questions);

			short answerRRs = buffer.getShort();
			header.setAnswerRRs(answerRRs);
			logger.info("Answer RRs : " + answerRRs);

			short authorityRRs = buffer.getShort();
			header.setAuthorityRRs(authorityRRs);
			logger.info("Authority RRs : " + authorityRRs);

			short additionalRRs = buffer.getShort();
			header.setAdditionalRRs(additionalRRs);
			logger.info("Additional RRs: " + additionalRRs);

			dnsReply.setHeader(header);
			decodeBody_Query(replyBuffer, buffer, (int) dnsReply.getHeader().getQuestions(), dnsReply);
		} catch (UnknownHostException e) {
			logger.error("No such name");
		}
	}

	private void decodeBody_Query(byte[] replyBuffer, ByteBuffer buffer,
			int numOfField, DnsReply dnsReply) {
		if (numOfField == 0) {
			DnsAnswerDecoder answerDecoder = new DnsAnswerDecoder();
			answerDecoder.decodeBody_Answers(dnsReply, replyBuffer, buffer,
					(int) dnsReply.getHeader().getAnswerRRs());
		} else {
			logger.info("Decoding Queries");
			DnsQuery query = new DnsQuery();

			DnsNameParser dnsNameParser = new DnsNameParser();
			String domainName = dnsNameParser.parse(replyBuffer, buffer,
					dnsNameParser.calcDomainLength(buffer));
			query.setDomainName(domainName);
			logger.info("Name server : " + domainName);

			short queryType = buffer.getShort();
			query.setQueryType(queryType);
			logger.info("Query Type : " + queryType);

			short queryClass = buffer.getShort();
			query.setQueryClass(queryClass);
			logger.info("Query Class : " + queryClass);

			dnsReply.setQueries(query);
			decodeBody_Query(replyBuffer, buffer, numOfField - 1, dnsReply);
		}
	}
}