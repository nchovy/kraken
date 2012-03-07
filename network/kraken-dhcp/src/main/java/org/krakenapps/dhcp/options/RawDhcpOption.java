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
package org.krakenapps.dhcp.options;

import org.krakenapps.dhcp.DhcpOption;

public class RawDhcpOption implements DhcpOption {
	private byte type;
	private int length;
	private byte[] value;

	public RawDhcpOption(int type, int value) {
		this((byte) type, 1, new byte[] { (byte) value });
	}

	public RawDhcpOption(int type, int length, byte[] value) {
		this.type = (byte) type;
		this.length = length;
		this.value = value;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("DHCP Option (t=%d,l=%d)", type, length);
	}
}
