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

/**
 * @author mindori
 */
public class DnsHeader {
	private short transactionId;
	private DnsFlags flags;
	private short questions;
	private short answerRRs;
	private short authorityRRs;
	private short additionalRRs;

	public DnsFlags getFlags() {
		return flags;
	}

	public void setFlags(DnsFlags flags) {
		this.flags = flags;
	}

	public short getQuestions() {
		return questions;
	}

	public void setQuestions(short questions) {
		this.questions = questions;
	}

	public short getAnswerRRs() {
		return answerRRs;
	}

	public void setAnswerRRs(short answerRRs) {
		this.answerRRs = answerRRs;
	}

	public short getAuthorityRRs() {
		return authorityRRs;
	}

	public void setAuthorityRRs(short authorityRRs) {
		this.authorityRRs = authorityRRs;
	}

	public short getAdditionalRRs() {
		return additionalRRs;
	}

	public void setAdditionalRRs(short additionalRRs) {
		this.additionalRRs = additionalRRs;
	}

	public short getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(short transactionId) {
		this.transactionId = transactionId;
	}
}
