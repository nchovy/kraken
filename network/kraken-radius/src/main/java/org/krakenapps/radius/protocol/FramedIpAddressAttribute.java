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

import java.net.InetAddress;
import java.net.UnknownHostException;

public class FramedIpAddressAttribute extends RadiusAttribute {

	private InetAddress ip;
	
	public FramedIpAddressAttribute(InetAddress ip) {
		this.ip = ip;
	}
	
	public FramedIpAddressAttribute(byte[] encoded, int offset, int length) throws UnknownHostException {
		if (encoded[offset] != getType())
			throw new IllegalArgumentException("binary is not framed ip address attribute");
		
		this.ip = decodeIp(encoded, offset, length);
	}
	
	@Override
	public int getType() {
		return 8;
	}

	public InetAddress getIp() {
		return ip;
	}
	
	@Override
	public byte[] getBytes() {
		return encodeIp(getType(), ip);
	}
}
