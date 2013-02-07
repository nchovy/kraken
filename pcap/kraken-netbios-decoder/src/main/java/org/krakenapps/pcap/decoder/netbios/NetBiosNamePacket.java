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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.krakenapps.pcap.Injectable;
import org.krakenapps.pcap.PacketBuilder;
import org.krakenapps.pcap.decoder.netbios.rr.AResourceRecord;
import org.krakenapps.pcap.decoder.netbios.rr.NbResourceRecord;
import org.krakenapps.pcap.decoder.netbios.rr.NbstatResourceRecord;
import org.krakenapps.pcap.decoder.netbios.rr.NsResourceRecord;
import org.krakenapps.pcap.decoder.netbios.rr.NullResourceRecord;
import org.krakenapps.pcap.decoder.netbios.rr.QuestionResourceRecord;
import org.krakenapps.pcap.decoder.netbios.rr.ResourceRecord;
import org.krakenapps.pcap.decoder.udp.UdpPacket;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

public class NetBiosNamePacket implements Injectable {
	private UdpPacket udpPacket;
	private NetBiosNameHeader header;
	private NetBiosNameData data;

	/* qeustion section */
	//	private List<QuestionResourceRecord> questions;

	/* resouce record */
	// private List<ResourceRecord> answerResources;
	// private List<ResourceRecord> authorityResources;
	// private List<ResourceRecord> additionalResources;
	//
	// private byte domainType;

	//

	public UdpPacket getUdpPacket() {
		return udpPacket;
	}

	public void setUdpPacket(UdpPacket udpPacket) {
		this.udpPacket = udpPacket;
	}

	public NetBiosNameHeader getHeader() {
		return header;
	}

	public NetBiosNameData getData() {
		return data;
	}

	public NetBiosNamePacket(NetBiosNameHeader header, NetBiosNameData data) {
		this.header = header;
		this.data = data;
	}

	@Override
	public String toString() {
		return header.toString() + data.toString();
	}

	@Override
	public Buffer getBuffer() {
		ByteBuffer headerb = ByteBuffer.allocate(12);
		// byteBuffer datab = ByteBuffer.allocate(arg0)
		headerb.putShort((short) header.getTransactionId());
		headerb.putShort((short) header.decodeOpCode());
		headerb.putShort((short) header.getQuestionCount());
		headerb.putShort((short) header.getAnswerCount());
		headerb.putShort((short) header.getAuthorityCount());
		headerb.putShort((short) header.getAdditionalCoount());

		Buffer buffer = new ChainBuffer();
		// TODO : data add
		buffer.addLast(headerb.array());
		return buffer;
	}

	public static class Builder implements PacketBuilder {
		NetBiosNameHeader header;
		NetBiosNameData data;
		// header variable//
		private short transactionId;
		private short opCode; // opcode + nmflag + resultcode
		//
		// private short op;
		// private short nmflag;
		// private short resultcode;
		// op+ nmflag + resultcode = opCode
		private short questionCount;// it must 0x0001
		private short answerCount;// it must 0x0001
		private short authorityCount;// it must 0x0001
		private short additionalCount;// it must 0x0001
		// header//

		/* qeustion section */
		private List<QuestionResourceRecord> questions;

		/* resouce record */
		private List<ResourceRecord> answerResources;
		private List<ResourceRecord> authorityResources;
		private List<ResourceRecord> additionalResources;

		private QuestionResourceRecord q;
		private String questionName;
		private byte domainType;
		private int cls;

		private ResourceRecord an;
		private String anName;
		private byte anType;

		private ResourceRecord au;
		private String auName;
		private byte auType;

		private ResourceRecord ad;
		private String adName;
		private byte adType;

		// data
		@Override
		public NetBiosNamePacket build() {
			header = NetBiosNameHeader.makeNameHeader();
			data = NetBiosNameData.makeNameData();
			// header set
			header.setTransactionId(transactionId);
			header.setOpcode(opCode);
			header.setQuestionCount(questionCount);
			header.setAnswerCount(answerCount);
			header.setAuthorityCount(authorityCount);
			header.setAdditionalCount(additionalCount);
			// header set

			if (questionCount > 0) {
				questions = new ArrayList<QuestionResourceRecord>(questionCount);
				q = new QuestionResourceRecord(questionName, domainType);
				q.setCls(cls);
				questions.add(q);
				data.setQuestions(questions);
			}
			if (answerCount > 0) {
				answerResources = new ArrayList<ResourceRecord>(answerCount);
				an = create(anName, anType);
				// add other variable
				answerResources.add(an);
				data.setAnswerResources(answerResources);
			}
			if (authorityCount > 0) {
				authorityResources = new ArrayList<ResourceRecord>(authorityCount);
				au = create(auName, auType);
				// add other variable
				authorityResources.add(au);
				data.setAuthorityResources(authorityResources);
			}
			if (additionalCount > 0) {
				additionalResources = new ArrayList<ResourceRecord>(additionalCount);
				ad = create(adName, adType);
				// add other variable
				additionalResources.add(ad);
				data.setAdditionalResources(additionalResources);
			}
			NetBiosNamePacket p = new NetBiosNamePacket(header, data);
			return p;
		}

		@Override
		public Object getDefault(String arg0) {
			return null;
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

	}

}
