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
package org.krakenapps.winapi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class ArpEntry {
	public enum Type {
		Static, Dynamic, Invalid, Other
	};

	private static List<AdapterInfo> infos;

	private int adapterIndex;
	private String adapterName;
	private byte[] physicalAddress;
	private InetAddress address;
	private Type type;

	private ArpEntry(int adapterIndex, byte[] physicalAddress, byte[] address, String type) throws UnknownHostException {
		if (infos == null)
			infos = AdapterInfo.getAdapterInfos();

		this.adapterIndex = adapterIndex;
		for (AdapterInfo info : infos) {
			if (adapterIndex == info.getIndex())
				adapterName = info.getDescription();
		}
		this.physicalAddress = physicalAddress;
		this.address = InetAddress.getByAddress(address);
		this.type = Type.valueOf(type);
	}

	public int getAdapterIndex() {
		return adapterIndex;
	}

	public String getAdapterName() {
		return adapterName;
	}

	public byte[] getPhysicalAddress() {
		return physicalAddress;
	}

	public InetAddress getAddress() {
		return address;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		byte[] b = physicalAddress;
		String mac = "";
		if (b != null && b.length == 6)
			mac = String.format(", mac=%02x:%02x:%02x:%02x:%02x:%02x", b[0], b[1], b[2], b[3], b[4], b[5]);

		return String.format("adapterIndex=%d, adapterName=%s, type=%s, ip=%s%s", adapterIndex, adapterName, type,
				address.getHostAddress(), mac);
	}

}
