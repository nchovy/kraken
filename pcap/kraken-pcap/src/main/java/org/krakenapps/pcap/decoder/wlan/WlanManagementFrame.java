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

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.decoder.wlan.tag.TaggedParameter;

public abstract class WlanManagementFrame extends WlanFrame {
	protected short duration;
	protected MacAddress dst;
	protected MacAddress src;
	protected MacAddress bssid;
	protected int fragment;
	protected int seq;
	protected int frameCheckSeq;

	// 8bytes
	protected long timestamp;

	// 2bytes
	protected int beaconInterval;

	// 2bytes
	protected short capabilities;

	protected List<TaggedParameter> parameters = new ArrayList<TaggedParameter>();

	public short getDuration() {
		return duration;
	}

	public void setDuration(short duration) {
		this.duration = duration;
	}

	public MacAddress getDestination() {
		return dst;
	}

	public void setDestination(MacAddress dst) {
		this.dst = dst;
	}

	public MacAddress getSource() {
		return src;
	}

	public void setSource(MacAddress src) {
		this.src = src;
	}

	public MacAddress getBssid() {
		return bssid;
	}

	public void setBssid(MacAddress bssid) {
		this.bssid = bssid;
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

	public int getFrameCheckSeq() {
		return frameCheckSeq;
	}

	public void setFrameCheckSeq(int frameCheckSeq) {
		this.frameCheckSeq = frameCheckSeq;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getBeaconInterval() {
		return beaconInterval;
	}

	public void setBeaconInterval(int beaconInterval) {
		this.beaconInterval = beaconInterval;
	}

	public short getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(short capabilities) {
		this.capabilities = capabilities;
	}

	public List<TaggedParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<TaggedParameter> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return "Management Frame" + parameters.toString();
	}
}
