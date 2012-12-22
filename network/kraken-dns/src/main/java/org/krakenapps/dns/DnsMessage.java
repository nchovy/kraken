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
 * @author xeraph@nchovy.com
 */
public class DnsMessage {
	private int id;
	private DnsFlags flags = new DnsFlags();
	private int questionCount;
	private int answerCount;
	private int authorityCount;
	private int additionalCount;

	private List<DnsResourceRecord> questions = new ArrayList<DnsResourceRecord>();
	private List<DnsResourceRecord> answers = new ArrayList<DnsResourceRecord>();
	private List<DnsResourceRecord> authorities = new ArrayList<DnsResourceRecord>();
	private List<DnsResourceRecord> additionals = new ArrayList<DnsResourceRecord>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public DnsFlags getFlags() {
		return flags;
	}

	public void setFlags(DnsFlags flags) {
		this.flags = flags;
	}

	public int getQuestionCount() {
		return questionCount;
	}

	public void setQuestionCount(int questionCount) {
		this.questionCount = questionCount;
	}

	public int getAnswerCount() {
		return answerCount;
	}

	public void setAnswerCount(int answerCount) {
		this.answerCount = answerCount;
	}

	public int getAuthorityCount() {
		return authorityCount;
	}

	public void setAuthorityCount(int authorityCount) {
		this.authorityCount = authorityCount;
	}

	public int getAdditionalCount() {
		return additionalCount;
	}

	public void setAdditionalCount(int additionalCount) {
		this.additionalCount = additionalCount;
	}

	public List<DnsResourceRecord> getQuestions() {
		return questions;
	}

	public void addQuestion(DnsResourceRecord rr) {
		questions.add(rr);
	}

	public List<DnsResourceRecord> getAnswers() {
		return answers;
	}

	public void addAnswer(DnsResourceRecord rr) {
		answers.add(rr);
	}

	public List<DnsResourceRecord> getAuthorities() {
		return authorities;
	}

	public void addAuthority(DnsResourceRecord rr) {
		authorities.add(rr);
	}

	public List<DnsResourceRecord> getAdditionals() {
		return additionals;
	}

	public void addAdditional(DnsResourceRecord rr) {
		additionals.add(rr);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("ID: 0x%04x, ", id));
		sb.append(getFlags().toString());

		if (flags.isQuery()) {
			sb.append(toString(questions));
		} else {
			sb.append(toString(answers));
			sb.append(" ");
			sb.append(toString(authorities));
			sb.append(" ");
			sb.append(toString(additionals));
		}

		return sb.toString();
	}

	private String toString(List<DnsResourceRecord> rrs) {
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		for (DnsResourceRecord rr : rrs) {
			if (first)
				first = false;
			else
				sb.append(" ");

			sb.append(rr.toString());
		}

		return sb.toString();
	}
}
