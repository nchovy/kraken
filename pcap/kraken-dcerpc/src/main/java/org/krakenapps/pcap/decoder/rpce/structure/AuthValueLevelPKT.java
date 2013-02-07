/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;

public class AuthValueLevelPKT implements AuthValue {

	private byte subType;
	private byte checksumLength;
	private byte[] checksum;

	public byte getSubType() {
		return subType;
	}

	public void setSubType(byte subType) {
		this.subType = subType;
	}

	public byte getChecksumLength() {
		return checksumLength;
	}

	public void setChecksumLength(byte checksumLength) {
		this.checksumLength = checksumLength;
	}

	public byte[] getChecksum() {
		return checksum;
	}

	public void setChecksum(byte[] checksum) {
		this.checksum = checksum;
	}

	@Override
	public void parse(Buffer b) {
		subType = b.get();
		checksumLength = b.get();
		checksum = new byte[checksumLength];
		b.gets(checksum);
	}
}
