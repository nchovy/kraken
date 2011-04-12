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

import java.util.List;
import java.util.ArrayList;
/**
 * @author mindori
 */
public class DnsReply {
	private DnsHeader header;
	private List<DnsQuery> queries = new ArrayList<DnsQuery>();
	private List<DnsAnswer> answers = new ArrayList<DnsAnswer>();
	private List<DnsAuthoritativeNameServer> authoritativeNameServers = new ArrayList<DnsAuthoritativeNameServer>();
	private List<DnsAdditionalRecord> additionalRecords = new ArrayList<DnsAdditionalRecord>();

	public DnsHeader getHeader() {
		return header;
	}

	public void setHeader(DnsHeader header) {
		this.header = header;
	}

	public List<DnsQuery> getQueries() {
		return queries;
	}

	public void setQueries(DnsQuery query) {
		queries.add(query);
	}

	public List<DnsAnswer> getAnswers() {
		return answers;
	}

	public void setAnswers(DnsAnswer answer) {
		answers.add(answer);
	}

	public List<DnsAuthoritativeNameServer> getAuthoritativeNameServers() {
		return authoritativeNameServers;
	}

	public void setAuthoritativeNameServers(
			DnsAuthoritativeNameServer authoritativeNameServer) {
		authoritativeNameServers.add(authoritativeNameServer);
	}

	public List<DnsAdditionalRecord> getAdditionalRecords() {
		return additionalRecords;
	}

	public void setAdditionalRecords(DnsAdditionalRecord additionalRecord) {
		additionalRecords.add(additionalRecord);
	}
}
