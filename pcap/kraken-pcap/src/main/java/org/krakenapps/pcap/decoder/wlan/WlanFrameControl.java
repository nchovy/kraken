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

public class WlanFrameControl {
	public static final int ORDER = 1 << 7;
	public static final int WEP = 1 << 6;
	public static final int MORE_DATA = 1 << 5;
	public static final int POWER_MANAGEMENT = 1 << 4;
	public static final int RETRY = 1 << 3;
	public static final int MORE_FRAG = 1 << 2;
	public static final int FROM_DS = 1 << 1;
	public static final int TO_DS = 1;

	// 4bit
	private byte subtype;

	// 2bit
	private byte type;

	// 2bit
	private byte version;

	// 8bit
	private byte flags;

	public WlanFrameControl(short s) {
		byte b = (byte) ((s >> 8) & 0xff);
		subtype = (byte) ((b >> 4) & 0x0f);
		type = (byte) ((b >> 2) & 0x03);
		version = (byte) ((b & 0x3));
		flags = (byte) s;
	}

	public byte getSubtype() {
		return subtype;
	}

	public void setSubtype(byte subtype) {
		this.subtype = subtype;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "type=" + type + ", subtype=" + subtype + ", version=" + version + ", flags="
				+ Integer.toHexString(flags);
	}
}
