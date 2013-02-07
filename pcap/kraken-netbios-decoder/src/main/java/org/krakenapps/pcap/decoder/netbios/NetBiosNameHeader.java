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

import org.krakenapps.pcap.util.Buffer;

public class NetBiosNameHeader {
	public enum Opcode {
		Unknown(-1), Query(0), Registration(5), Release(6), Wack(7), Refresh(8);

		private int code;

		Opcode(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static Opcode parse(int value) {
			for (Opcode opcode : values())
				if (opcode.getCode() == value)
					return opcode;

			return Unknown;
		}
	}

	// rcode
	public enum ResultCode {
		Unknown(-1), NoError(0), FormatError(1), ServerFailure(2), UnsupportedRequestError(4), RefulsedError(5), ActiveError(
				6), ConflictError(7);

		private int value;

		ResultCode(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static ResultCode parse(int value) {
			for (ResultCode code : values())
				if (code.getValue() == value)
					return code;

			return Unknown;
		}
	}

	// query or response bit
	public static final short ResponseMask = (short) 0x10; // 10000000 ,
	// 00000000

	// opcode field
	public static final short OpcodeMask = (short) 0x0f;

	// NM_flags field
	public static final short Broadcast = (short) 0x01;// 00000000 , 00010000
	public static final short RecursionAvailable = (short) 0x08;// 00000000 ,
	// 10000000
	public static final short RecursionDesired = (short) 0x10;// 00000001 ,
	// 00000000
	public static final short Truncation = (short) 0x20;// 00000010 , 00000000
	public static final short AuthoritativeAnswer = (short) 0x40;// 00000100 ,
	// 00000000
	// RCODE ERR
	public static final short FMT_ERR = (short) 0x01;
	public static final short SRV_ERR = (short) 0x02;
	public static final short IMP_ERR = (short) 0x04;
	public static final short RFS_ERR = (short) 0x05;
	public static final short ACT_ERR = (short) 0x06;
	public static final short CFT_ERR = (short) 0x07;

	/* header */
	private int transactionId = 0;

	private boolean isResponse;

	// 5bit
	private Opcode opcode;
	// 7bit
	private int nmFlags;
	// 4bit
	private ResultCode resultCode;

	// questions
	private int qdCount = 0;

	// answers
	private int anCount = 0;

	// authorities
	private int nsCount = 0;

	// additionals
	private int arCount = 0;

	/* header */
	private NetBiosNameHeader() {
	}

	public Opcode getOpcode() {
		return opcode;
	}

	public ResultCode getResultCode() {
		return resultCode;
	}

	public boolean isAuthoritativeAnswer() {
		return (nmFlags & AuthoritativeAnswer) == AuthoritativeAnswer;
	}

	public boolean isTruncation() {
		return (nmFlags & Truncation) == Truncation;
	}

	public boolean isRecursionDesired() {
		return (nmFlags & RecursionDesired) == RecursionDesired;
	}

	public boolean isRecursionAvailable() {
		return (nmFlags & RecursionAvailable) == RecursionAvailable;
	}

	public boolean isResponse() {
		return isResponse;
	}

	public int getQuestionCount() {
		return qdCount;
	}

	public int getAnswerCount() {
		return anCount;
	}

	public int getAdditionalCoount() {
		return arCount;
	}

	public int getAuthorityCount() {
		return nsCount;
	}

	public void setTransactionId(short id) {
		transactionId = id & 0xffff;
	}

	public void setQuestionCount(int count) {
		qdCount = count;
	}

	public void setAnswerCount(int count) {
		anCount = count;
	}

	public void setAdditionalCount(int count) {
		arCount = count;
	}

	public void setAuthorityCount(int count) {
		nsCount = count;
	}

	public int getTransactionId() {
		return transactionId;
	}

	public void setOpcode(short opcode) {
		this.opcode = Opcode.parse(opcode & 0xffff);
	}

	public static NetBiosNameHeader parse(Buffer b) {
		NetBiosNameHeader header = new NetBiosNameHeader();
		header.setTransactionId(b.getShort());
		header.parseOpCode(b.getShort());
		header.setQuestionCount(b.getShort());
		header.setAnswerCount(b.getShort());
		header.setAuthorityCount(b.getShort());
		header.setAdditionalCount(b.getShort());
		return header;
	}

	// this function is called in only netbiosNamePacket class
	public static NetBiosNameHeader makeNameHeader() {
		return new NetBiosNameHeader();
	}

	//
	private void parseOpCode(short value) {
		int op = (value >> 11) & 0xff;
		isResponse = (op & ResponseMask) == ResponseMask;
		this.opcode = Opcode.parse(op);
		this.nmFlags = (value >> 4) & 0x7f;
		this.resultCode = ResultCode.parse((value) & 0x0f);
		/*
		 * if( (!isResponse) && (this.resultCode != ResultCode.NoError)) { throw
		 * new
		 * IllegalArgumentException("Query packet is can't contained error Code = "
		 * + this.resultCode); }
		 */
	}

	public short decodeOpCode() {
		short value;
		value = ((short) (this.opcode.getCode() << 11));
		value = (short) (value | (short) (this.nmFlags << 4));
		value = (short) (value | (short) (this.resultCode.getValue()));
		return value;

	}

	@Override
	public String toString() {
		return String.format("NetBiosNameHeader \n"
				+ "tx=0x%x, response=%s, opcode=%s, "
				+ "nmflag=0x%x, result=%s, qd=%d, an=%d, ns=%d, ar=%d\n", this.transactionId, this.isResponse,
				this.opcode, this.nmFlags, this.resultCode, this.qdCount, this.anCount, this.nsCount, this.arCount);
	}
}
