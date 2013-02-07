/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.pcap.decoder.msn;

import java.util.Properties;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ChainBuffer;

/**
 * @author mindori
 */
public class MsnSession {
	private Buffer txBuffer;
	private Buffer rxBuffer;
	private Properties props;

	private String msnUserAddress;
	private byte[] truncatedMsgData;

	private int msnPayloadLength = -1;
	private int arrivedPayloadLength = 0;
	private int remainDataLength = -1;
	private int declaredPayloadLength = -1;

	private boolean isGetDeclaredPayloadLength = false;
	private boolean isTruncated = false;

	public MsnSession() {
		txBuffer = new ChainBuffer();
		rxBuffer = new ChainBuffer();
		props = new Properties();
	}

	public Buffer getTxBuffer() {
		return txBuffer;
	}

	public Buffer getRxBuffer() {
		return rxBuffer;
	}

	public Properties getProps() {
		return props;
	}

	public String getMsnUserAddress() {
		return msnUserAddress;
	}

	public void setMsnUserAddress(String msnUserAddress) {
		this.msnUserAddress = msnUserAddress;
	}

	public byte[] getTruncatedMsgData() {
		return truncatedMsgData;
	}

	public void setTruncatedMsgData(byte[] truncatedMsnData) {
		this.truncatedMsgData = truncatedMsnData;
	}

	public int getMsnPayloadLength() {
		return msnPayloadLength;
	}

	public void setMsnPayloadLength(int msnPayloadLength) {
		this.msnPayloadLength = msnPayloadLength;
	}

	public int getArrivedPayloadLength() {
		return arrivedPayloadLength;
	}

	public void setArrivedPayloadLength(int arrivedPayloadLength) {
		this.arrivedPayloadLength = arrivedPayloadLength;
	}

	public int getRemainDataLength() {
		return remainDataLength;
	}

	public void setRemainDataLength(int remainDataLength) {
		this.remainDataLength = remainDataLength;
	}

	public int getDeclaredPayloadLength() {
		return declaredPayloadLength;
	}

	public void setDeclaredPayloadLength(int declaredPayloadLength) {
		this.declaredPayloadLength = declaredPayloadLength;
	}

	public boolean isGetDeclaredPayloadLength() {
		return isGetDeclaredPayloadLength;
	}

	public void setGetDeclaredPayloadLength(boolean isGetDeclaredPayloadLength) {
		this.isGetDeclaredPayloadLength = isGetDeclaredPayloadLength;
	}

	public boolean isTruncated() {
		return isTruncated;
	}

	public void setTruncated(boolean isTruncated) {
		this.isTruncated = isTruncated;
	}

	public void initTruncatedInstance() {
		truncatedMsgData = null;
		isTruncated = false;
	}

	public void clear() {
		txBuffer = null;
		rxBuffer = null;
		
		txBuffer = new ChainBuffer();
		rxBuffer = new ChainBuffer();

		truncatedMsgData = null;

		msnPayloadLength = -1;
		arrivedPayloadLength = 0;
		remainDataLength = -1;

		isGetDeclaredPayloadLength = false;
		isTruncated = false;
	}
}
