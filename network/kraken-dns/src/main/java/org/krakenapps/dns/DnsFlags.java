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
public class DnsFlags {
	private boolean isQuery;
	private byte opCode;
	private boolean isAuthoritativeAnswer;
	private boolean isTruncated;
	private boolean isRecursionDesired;
	private boolean isRecursionAvailable;
	// reserved bit(== 0)
	private boolean isAnswerAuthenticated;
	private boolean isNonAuthenticatedDataOK;
	private DnsResponseCode responseCode = DnsResponseCode.NO_ERROR;

	public DnsFlags() {
	}

	public DnsFlags(short bits) {
		setBits(bits);
	}

	public short getBits() {
		short bits = 0;
		bits |= bit(!isQuery) << 15;
		bits |= opCode << 11;
		bits |= bit(isAuthoritativeAnswer) << 10;
		bits |= bit(isTruncated) << 9;
		bits |= bit(isRecursionDesired) << 8;
		bits |= bit(isRecursionAvailable) << 7;
		bits |= bit(isAnswerAuthenticated) << 5;
		bits |= bit(isNonAuthenticatedDataOK) << 4;
		bits |= responseCode.getCode();
		return bits;
	}

	private int bit(boolean b) {
		return b ? 1 : 0;
	}

	public void setBits(short bits) {
		short s = bits;

		setQuery(((s & 0x8000) >> 15) == 0);
		setOpCode((byte) ((s & 0x7800) >> 11));
		setAuthoritativeAnswer(((s & 0x0400) >> 10) != 0);
		setTruncated(((s & 0x0200) >> 9) != 0);
		setRecursionDesired(((s & 0x0100) >> 8) != 0);
		setRecursionAvailable(((s & 0x0080) >> 7) != 0);
		setAnswerAuthenticated(((s & 0x0020) >> 5) != 0);
		setNonAuthenticatedDataOK(((s & 0x0010) >> 4) != 0);

		DnsResponseCode rcode = DnsResponseCode.parse(s & 0xf);
		if (rcode == null)
			throw new IllegalStateException("invalid response code: " + (s & 0xf));

		setResponseCode(rcode);
	}

	public boolean isQuery() {
		return isQuery;
	}

	public void setQuery(boolean isQuery) {
		this.isQuery = isQuery;
	}

	public byte getOpCode() {
		return opCode;
	}

	public void setOpCode(byte opCode) {
		if (opCode < 0 || opCode > 2)
			throw new IllegalArgumentException("invalid dns opcode: " + opCode);

		this.opCode = opCode;
	}

	public boolean isAuthoritativeAnswer() {
		return isAuthoritativeAnswer;
	}

	public void setAuthoritativeAnswer(boolean isAuthoritativeAnswer) {
		this.isAuthoritativeAnswer = isAuthoritativeAnswer;
	}

	public boolean isTruncated() {
		return isTruncated;
	}

	public void setTruncated(boolean isTruncated) {
		this.isTruncated = isTruncated;
	}

	public boolean isRecursionDesired() {
		return isRecursionDesired;
	}

	public void setRecursionDesired(boolean isRecursionDesired) {
		this.isRecursionDesired = isRecursionDesired;
	}

	public boolean isRecursionAvailable() {
		return isRecursionAvailable;
	}

	public void setRecursionAvailable(boolean isRecursionAvailable) {
		this.isRecursionAvailable = isRecursionAvailable;
	}

	public boolean isAnswerAuthenticated() {
		return isAnswerAuthenticated;
	}

	public void setAnswerAuthenticated(boolean isAnswerAuthenticated) {
		this.isAnswerAuthenticated = isAnswerAuthenticated;
	}

	public boolean isNonAuthenticatedDataOK() {
		return isNonAuthenticatedDataOK;
	}

	public void setNonAuthenticatedDataOK(boolean isNonAuthenticatedDataOK) {
		this.isNonAuthenticatedDataOK = isNonAuthenticatedDataOK;
	}

	public DnsResponseCode getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(DnsResponseCode responseCode) {
		this.responseCode = responseCode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (opCode == 0)
			sb.append("standard query");
		else if (opCode == 1)
			sb.append("inverse query");
		else if (opCode == 2)
			sb.append("server status request");

		if (!isQuery)
			sb.append(" response ");
		else
			sb.append(" ");

		String flagsDesc = getFlagsDesc();
		if (!flagsDesc.isEmpty()) {
			sb.append("(");
			sb.append(flagsDesc);
			sb.append(") ");
		}

		return sb.toString();
	}

	private String getFlagsDesc() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;

		if (isAuthoritativeAnswer) {
			if (first)
				first = false;
			else
				sb.append(" ");
			sb.append("AA");
		}

		if (isTruncated) {
			if (first)
				first = false;
			else
				sb.append(" ");
			sb.append("TC");
		}

		if (isRecursionDesired) {
			if (first)
				first = false;
			else
				sb.append(" ");
			sb.append("RD");
		}

		if (isRecursionAvailable) {
			if (first)
				first = false;
			else
				sb.append(" ");
			sb.append("RA");
		}

		if (responseCode != DnsResponseCode.NO_ERROR) {
			if (first)
				first = false;
			else
				sb.append(" ");
			
			sb.append(responseCode.getDescription());
		}

		return sb.toString();
	}

}
