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

import org.krakenapps.dns.rr.A;

/**
 * @author mindori
 */
public class DnsMessageCodec {
	private DnsMessageCodec() {
	}

	public static ByteBuffer encode(DnsMessage msg) {
		ByteBuffer bb = ByteBuffer.allocate(65536);

		bb.putShort((short) msg.getId());
		bb.putShort(msg.getFlags().getBits());
		bb.putShort((short) msg.getQuestionCount());
		bb.putShort((short) msg.getAnswerCount());
		bb.putShort((short) msg.getAuthorityCount());
		bb.putShort((short) msg.getAdditionalCount());

		for (DnsResourceRecord rr : msg.getQuestions())
			encodeResourceRecord(bb, rr);

		for (DnsResourceRecord rr : msg.getAnswers())
			encodeResourceRecord(bb, rr);

		for (DnsResourceRecord rr : msg.getAuthorities())
			encodeResourceRecord(bb, rr);

		for (DnsResourceRecord rr : msg.getAdditionals())
			encodeResourceRecord(bb, rr);

		bb.flip();

		return bb;
	}

	public static DnsMessage decode(ByteBuffer bb) {
		DnsMessage msg = new DnsMessage();

		msg.setId(bb.getShort() & 0xffff);
		msg.setFlags(new DnsFlags(bb.getShort()));
		msg.setQuestionCount(bb.getShort() & 0xffff);
		msg.setAnswerCount(bb.getShort() & 0xffff);
		msg.setAuthorityCount(bb.getShort() & 0xffff);
		msg.setAdditionalCount(bb.getShort() & 0xffff);

		for (int i = 0; i < msg.getQuestionCount(); i++) {
			DnsResourceRecord rr = decodeResourceRecord(bb);
			if (rr != null)
				msg.addQuestion(rr);
		}

		for (int i = 0; i < msg.getAnswerCount(); i++) {
			DnsResourceRecord rr = decodeResourceRecord(bb);
			if (rr != null)
				msg.addAnswer(rr);
		}

		for (int i = 0; i < msg.getAuthorityCount(); i++) {
			DnsResourceRecord rr = decodeResourceRecord(bb);
			if (rr != null)
				msg.addAuthority(rr);
		}

		for (int i = 0; i < msg.getAdditionalCount(); i++) {
			DnsResourceRecord rr = decodeResourceRecord(bb);
			if (rr != null)
				msg.addAdditional(rr);
		}

		return msg;
	}

	private static void encodeResourceRecord(ByteBuffer bb, DnsResourceRecord rr) {
		DnsLabelCodec.encode(bb, rr.getName());
		bb.putShort((short) rr.getType());
		bb.putShort((short) rr.getClazz());
		rr.encode(bb);
	}

	private static DnsResourceRecord decodeResourceRecord(ByteBuffer bb) {
		String label = DnsLabelCodec.decode(bb);
		int type = bb.getShort() & 0xffff;
		int clazz = bb.getShort() & 0xffff;

		DnsResourceRecord rr = null;
		switch (type) {
		case 1:
			rr = A.decode(bb);
			break;
		default:
			System.out.println("rr type " + type + " not supported");
			break;
		}

		if (rr != null) {
			rr.setName(label);
			rr.setType(type);
			rr.setClazz(clazz);
		}

		return rr;
	}
}