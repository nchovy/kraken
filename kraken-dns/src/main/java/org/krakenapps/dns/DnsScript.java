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

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;

/**
 * @author mindori
 */

public class DnsScript implements Script {
	private ScriptContext context;

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void dig(String[] args) {
		DnsResolver dnsResolver = new DnsResolver();
		DnsReply reply = dnsResolver.dig(args);

		DnsHeader header = reply.getHeader();
		DnsFlags flags = header.getFlags();
		context.printf("Transaction ID: %d\n", header.getTransactionId());
		context.println("");
		
		context.println("Flags:");
		context.println("\tResponse: " + !flags.isQuery());
		context.printf("\tOpcode: 0x%x\n", flags.getOpCode());
		context.println("\tAuthoritative: " + flags.isAuthoritativeAnswer());
		context.println("\tTruncated: " + flags.isTruncated());
		context.println("\tRecursion desired: " + flags.isRecursionDesired());
		context.println("\tRecursion available: " + flags.isRecursionAvailable());
		context.printf("\tReply Code: 0x%x\n", flags.getReplyCode());
		context.println("");
		
		context.println("Questions: " + header.getQuestions());
		context.println("Answer RRs: " + header.getAnswerRRs());
		context.println("Authority RRs: " + header.getAuthorityRRs());
		context.println("Additional RRs: " + header.getAdditionalRRs());
		context.println("");

		context.println("Queries:");
		for (DnsQuery query : reply.getQueries()) {
			context.println("\t" + query.toString());
			context.println("");
		}

		context.println("Answers:");
		for (DnsAnswer answer : reply.getAnswers()) {
			context.println(getAnswer(answer));
		}

		context.println("Authoritative nameservers:");
		for (DnsAuthoritativeNameServer server : reply.getAuthoritativeNameServers()) {
			context.println(getAuthoritativeNameServer(server));
		}

		context.println("");
		
		context.println("Additional records:");
		for (DnsAdditionalRecord record : reply.getAdditionalRecords()) {
			context.println(getAdditionalRecord(record));
		}
	}

	private String getAnswer(DnsAnswer answer) {
		return String.format("\tName: %s\r\n" + "\tType: %d\r\n" + "\tClass: 0x%x\r\n" + "\tTime to live: %d\r\n"
				+ "\tData length: %d\r\n" + "\tPrimary name server: %s\r\n" + "\tMail Exchange: %s\r\n", answer
				.getDomainAddress(), answer.getQueryType(), answer.getQueryClass(), answer.getTimeToLive(), answer
				.getDataLength(), answer.getNameServer(), answer.getMailExchange());
	}

	private String getAuthoritativeNameServer(DnsAuthoritativeNameServer server) {
		return String.format("\tName server: %s", server.getNameServer());
	}
	
	private String getAdditionalRecord(DnsAdditionalRecord record) {
		return String.format("\tAddress: %s", record.getDomainAddress());
	}
}
