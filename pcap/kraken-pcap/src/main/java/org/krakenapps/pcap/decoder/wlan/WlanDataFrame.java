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

import org.krakenapps.pcap.decoder.ethernet.MacAddress;

public class WlanDataFrame extends WlanFrame {

	// 2 bytes
	private int duration;

	// 6 bytes
	private MacAddress destination;

	// 6 bytes
	private MacAddress bssid;

	private MacAddress source;

	// 4 bit
	private int fragment;

	// 12 bit
	private int seq;

	// wep parameters (3 bytes)
	private int iv;

	// wep key index (1 byte)
	private int keyIndex;

	private byte[] data;

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public MacAddress getDestination() {
		return destination;
	}

	public void setDestination(MacAddress destination) {
		this.destination = destination;
	}

	public MacAddress getBssid() {
		return bssid;
	}

	public void setBssid(MacAddress bssid) {
		this.bssid = bssid;
	}

	public MacAddress getSource() {
		return source;
	}

	public void setSource(MacAddress source) {
		this.source = source;
	}

	public int getFragment() {
		return fragment;
	}

	public void setFragment(int fragment) {
		this.fragment = fragment;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public int getIv() {
		return iv;
	}

	public void setIv(int iv) {
		this.iv = iv;
	}

	public int getKeyIndex() {
		return keyIndex;
	}

	public void setKeyIndex(int keyIndex) {
		this.keyIndex = keyIndex;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public String toString() {
		int dataLen = 0;
		if (data != null)
			dataLen = data.length;
		return "Data [duration=" + duration + ", dst=" + destination + ", bssid=" + bssid + ", src=" + source
				+ ", seq=" + seq + ", fragment=" + fragment + ", iv=" + iv + ", key index=" + keyIndex + ", data len="
				+ dataLen + "]";
	}

}
