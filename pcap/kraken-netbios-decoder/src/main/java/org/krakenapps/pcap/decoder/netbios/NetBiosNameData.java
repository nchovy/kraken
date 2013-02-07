/*
 * Copyright 2011 Future Systems, Inc
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
package org.krakenapps.pcap.decoder.netbios;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.pcap.decoder.netbios.rr.AResourceRecord;
import org.krakenapps.pcap.decoder.netbios.rr.QuestionResourceRecord;
import org.krakenapps.pcap.decoder.netbios.rr.NbResourceRecord;
import org.krakenapps.pcap.decoder.netbios.rr.NbstatResourceRecord;
import org.krakenapps.pcap.decoder.netbios.rr.NullResourceRecord;
import org.krakenapps.pcap.decoder.netbios.rr.NsResourceRecord;
import org.krakenapps.pcap.decoder.netbios.rr.ResourceRecord;
import org.krakenapps.pcap.util.Buffer;

public class NetBiosNameData {

	// record count maybe have to exact 0x0001
	// it use array because expendable
	/* qeustion section */
	private List<QuestionResourceRecord> questions;

	/* resouce record */
	private List<ResourceRecord> answerResources;
	private List<ResourceRecord> authorityResources;
	private List<ResourceRecord> additionalResources;

	private byte domainType;

	public NetBiosNameData() {
		answerResources = new ArrayList<ResourceRecord>();
		authorityResources = new ArrayList<ResourceRecord>();
		additionalResources = new ArrayList<ResourceRecord>();
	}

	public byte getDomainType() {
		return domainType;
	}

	public void setDomainType(byte domainType) {
		this.domainType = domainType;
	}

	public List<QuestionResourceRecord> getQuestions() {
		return questions;
	}

	public List<ResourceRecord> getAnswers() {
		return answerResources;
	}

	public List<ResourceRecord> getAuthorities() {
		return authorityResources;
	}

	public List<ResourceRecord> getAdditionals() {
		return additionalResources;
	}

	public void setQuestions(List<QuestionResourceRecord> questions) {
		this.questions = questions;
	}

	public void setAnswerResources(List<ResourceRecord> answerResources) {
		this.answerResources = answerResources;
	}

	public void setAuthorityResources(List<ResourceRecord> authorityResources) {
		this.authorityResources = authorityResources;
	}

	public void setAdditionalResources(List<ResourceRecord> additionalResources) {
		this.additionalResources = additionalResources;
	}

	private void parseQuestion(NetBiosNameHeader h, Buffer b) {
		int count = h.getQuestionCount();
		if (count <= 0)
			return;

		questions = new ArrayList<QuestionResourceRecord>(count);
		for (int i = 0; i < count; i++) {
			byte domainType = NetBiosNameCodec.decodeDomainType(b);
			String name = NetBiosNameCodec.readName(b);
			QuestionResourceRecord question = new QuestionResourceRecord(name, domainType);
			question.parse(b, 0);
		//	System.out.println("NetBios Name = " + name + "<"+Integer.toHexString(question.getDomainType())+">");
			questions.add(question);
		}
	}

	private ResourceRecord create(String name, int type) {
		ResourceRecord.Type t = ResourceRecord.Type.parse(type);

		switch (t) {
		case A:
			return new AResourceRecord(name);
		case NULL:
			return new NullResourceRecord(name);
		case NB:
			return new NbResourceRecord(name);
		case NBSTAT:
			return new NbstatResourceRecord(name);
		case NS:
			return new NsResourceRecord(name);
		default:
			throw new IllegalArgumentException("illegal resource record type: " + type);
		}
	}

	private void parseResource(NetBiosNameHeader h, Buffer b) {
		int type;
		// if 0x20 , 0x31 , 0x51 , 0x00 -> this return 2
		int ancount = (int) h.getAnswerCount();
		int nscount = (int) h.getAuthorityCount();
		int arcount = (int) h.getAdditionalCoount();

		if (ancount > 0) {
			byte domainType = NetBiosNameCodec.decodeDomainType(b);
			String name = NetBiosNameCodec.readName(b);
			type = b.getShort() & 0xffff;

			for (int i = 0; i < ancount; i++) {
				ResourceRecord rr = create(name, type);
				rr.parse(b, type);
				rr.setDomainType(domainType);
				answerResources.add(rr);
			}
		}

		if (nscount > 0) {
			byte domainType = NetBiosNameCodec.decodeDomainType(b);
			String name = NetBiosNameCodec.readName(b);
			type = b.getShort() & 0xffff;

			for (int i = 0; i < nscount; i++) {
				ResourceRecord rr = create(name, type);
				rr.parse(b, type);
				rr.setDomainType(domainType);
				authorityResources.add(rr);
			}
		}

		if (arcount > 0) {
			byte domainType = NetBiosNameCodec.decodeDomainType(b);
			String name = NetBiosNameCodec.readName(b);
			type = b.getShort() & 0xffff;

			for (int i = 0; i < arcount; i++) {
				ResourceRecord rr = create(name, type);
				rr.parse(b, type);
				rr.setDomainType(domainType);
				additionalResources.add(rr);
			}
		}
	} // parseResourceField

	public static NetBiosNameData parse(NetBiosNameHeader header, Buffer b) {
		NetBiosNameData data = new NetBiosNameData();
		data.parseQuestion(header, b);
		data.parseResource(header, b);
		return data;
	}

	public static NetBiosNameData makeNameData() {
		return new NetBiosNameData();
	}

	@Override
	public String toString() {
		return String.format("NetBiosNameData domain type=0x%x", this.domainType);
		//return questions.toString() /*+ answerResources.toString() + authorityResources.toString() + additionalResources.toString()*/;
	}
}
