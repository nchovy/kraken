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
package org.krakenapps.radius.protocol;

public class FramedAppleTalkLinkAttribute extends RadiusAttribute {

	private int serialLink;

	public FramedAppleTalkLinkAttribute(int serialLink) {
		setSerialLink(serialLink);
	}

	public FramedAppleTalkLinkAttribute(byte[] encoded, int offset, int length) {
		if (encoded[offset] != getType())
			throw new IllegalArgumentException("binary is not framed appletalk link attribute");
		
		setSerialLink(decodeInt(encoded, offset, length));
	}
	
	@Override
	public int getType() {
		return 37;
	}

	private void setSerialLink(int serialLink) {
		if (serialLink < 0 || serialLink > 65535)
			throw new IllegalArgumentException("serial link value should be integer between 0 and 65535");
		
		this.serialLink = serialLink;
	}

	public int getSerialLink() {
		return serialLink;
	}

	@Override
	public byte[] getBytes() {
		return encodeInt(getType(), serialLink);
	}
}
