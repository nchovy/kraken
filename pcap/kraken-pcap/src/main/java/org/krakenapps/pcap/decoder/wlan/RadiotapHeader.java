/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.pcap.decoder.wlan;

public class RadiotapHeader {

	// 2 bytes
	private int headerLength;

	// 4 bytes
	private int presentFlags;

	// 8 bytes
	private long macTimestamp;

	// 1 byte
	private byte flags;

	// 1 byte
	private byte dataRate;

	// 2 bytes
	private short channelFrequency;

	// 2 bytes
	private short channelType;

	// 1 byte
	private byte ssiSignal;

	// 1 byte
	private byte ssiNoise;

	// 1 byte
	private byte antenna;

	// 1 byte
	private byte ssiSignal2;

	public int getHeaderLength() {
		return headerLength;
	}

	public void setHeaderLength(int headerLength) {
		this.headerLength = headerLength;
	}

	public int getPresentFlags() {
		return presentFlags;
	}

	public void setPresentFlags(int presentFlags) {
		this.presentFlags = presentFlags;
	}

	public long getMacTimestamp() {
		return macTimestamp;
	}

	public void setMacTimestamp(long macTimestamp) {
		this.macTimestamp = macTimestamp;
	}

	public byte getFlags() {
		return flags;
	}

	public void setFlags(byte flags) {
		this.flags = flags;
	}

	public byte getDataRate() {
		return dataRate;
	}

	public void setDataRate(byte dataRate) {
		this.dataRate = dataRate;
	}

	public short getChannelFrequency() {
		return channelFrequency;
	}

	public void setChannelFrequency(short channelFrequency) {
		this.channelFrequency = channelFrequency;
	}

	public short getChannelType() {
		return channelType;
	}

	public void setChannelType(short channelType) {
		this.channelType = channelType;
	}

	public byte getSsiSignal() {
		return ssiSignal;
	}

	public void setSsiSignal(byte ssiSignal) {
		this.ssiSignal = ssiSignal;
	}

	public byte getSsiNoise() {
		return ssiNoise;
	}

	public void setSsiNoise(byte ssiNoise) {
		this.ssiNoise = ssiNoise;
	}

	public byte getAntenna() {
		return antenna;
	}

	public void setAntenna(byte antenna) {
		this.antenna = antenna;
	}

	public byte getSsiSignal2() {
		return ssiSignal2;
	}

	public void setSsiSignal2(byte ssiSignal2) {
		this.ssiSignal2 = ssiSignal2;
	}

	@Override
	public String toString() {
		return "present flags=0x" + Integer.toHexString(presentFlags) + ", flags=0x" + Integer.toHexString(flags)
				+ ", data rate=" + dataRate + ", channel frequency=" + channelFrequency + ", channel type=0x"
				+ Integer.toHexString(channelType & 0xffff) + ", SSI signal=" + ssiSignal + "dBm, SSI noise="
				+ ssiNoise + "dBm, antenna=" + antenna + ", SSI signal2=" + ssiSignal2 + "dB";
	}
}
