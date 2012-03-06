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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private byte replyCode;
	private final Logger logger = LoggerFactory.getLogger(DnsFlags.class
			.getName());

	public DnsFlags(short flags) {
		setFlags(flags);
	}

	public DnsFlags() {

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

	public byte getReplyCode() {
		return replyCode;
	}

	public void setReplyCode(byte replyCode) {
		this.replyCode = replyCode;
	}

	private void setFlags(short flags) {
		short s = flags;
		int flagState;

		flagState = (s & 0x8000) >> 15;
		if (flagState == 0)
			setQuery(true);
		else
			setQuery(false);

		flagState = (s & 0x7800) >> 11;
		byte b = (byte) flagState;
		setOpCode(b);

		flagState = (s & 0x0400) >> 10;
		if (flagState == 0)
			setAuthoritativeAnswer(false);
		else
			setAuthoritativeAnswer(true);

		flagState = (s & 0x0200) >> 9;
		if (flagState == 0)
			setTruncated(false);
		else
			setTruncated(true);

		flagState = (s & 0x0100) >> 8;
		if (flagState == 0)
			setRecursionDesired(false);
		else
			setRecursionDesired(true);

		flagState = (s & 0x0080) >> 7;
		if (flagState == 0)
			setRecursionAvailable(false);
		else
			setRecursionAvailable(true);

		flagState = (s & 0x0020) >> 5;
		if (flagState == 0)
			setAnswerAuthenticated(false);
		else
			setAnswerAuthenticated(true);

		flagState = (s & 0x0010) >> 4;
		if (flagState == 0)
			setNonAuthenticatedDataOK(false);
		else
			setNonAuthenticatedDataOK(true);

		flagState = s & 0x000F;
		b = (byte) flagState;
		setReplyCode(b);
		printFlags();
	}

	private void printFlags() {
		logger.info("isQuery : " + isQuery());
		logger.info("opCode : " + getOpCode());
		logger.info("isAuthoritativeAnswer : " + isAuthoritativeAnswer());
		logger.info("isTruncated : " + isTruncated());
		logger.info("isRecursionDesired : " + isRecursionDesired());
		logger.info("isRecursionAvailable: " + isRecursionAvailable());
		logger.info("isAnswerAuthenticated : " + isAnswerAuthenticated());
		logger.info("isNotAuthenticatedDataOK : " + isNonAuthenticatedDataOK());
		logger.info("replyCode : " + getReplyCode());
	}
}
