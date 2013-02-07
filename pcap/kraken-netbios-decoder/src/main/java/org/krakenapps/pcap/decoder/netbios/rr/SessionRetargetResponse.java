/*
 * Copyright 2011 Future Systems, Inc
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
package org.krakenapps.pcap.decoder.netbios.rr;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.krakenapps.pcap.decoder.netbios.NetBiosSessionData;
import org.krakenapps.pcap.util.Buffer;

public class SessionRetargetResponse implements NetBiosSessionData {
	private InetAddress retargetIPAdress;
	private short port;

	private SessionRetargetResponse(InetAddress retargetAddress, short port) {
		this.retargetIPAdress = retargetAddress;
		this.port = port;
	}

	// this use retarget response packet only
	// this use request packet only
	public InetAddress getRetargetIPAdress() {
		return retargetIPAdress;
	}

	public short getPort() {
		return port;
	}

	public static NetBiosSessionData parse(Buffer b) {
		InetAddress retargetAddress = readIpAddress(b);
		short port = b.getShort();
		return new SessionRetargetResponse(retargetAddress, port);
	}

	private static InetAddress readIpAddress(Buffer b) {
		byte[] addr = new byte[4];
		b.gets(addr);
		try {
			return InetAddress.getByAddress(addr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Buffer getBuffer() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String toString(){
		return String.format("retargetIPAddress = %s , port = 0x%s\n",
				this.retargetIPAdress , Integer.toHexString(this.port));
	}

}
