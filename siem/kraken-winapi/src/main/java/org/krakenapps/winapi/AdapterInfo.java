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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author delmitz
 * 
 */
public class AdapterInfo {
	static {
		System.loadLibrary("winapi");
	}

	public enum Type {
		Other, Ethernet, Tokenring, FDDI, PPP, Loopback, Slip
	};

	private String name;
	private String description;
	private byte[] address;
	private int index;
	private Type type;
	private boolean dhcpEnabled;
	private boolean haveWins;

	private AdapterInfo(String name, String description, byte[] address, int index, int type, boolean dhcpEnabled,
			boolean haveWins) throws UnknownHostException {
		this.name = name;
		this.description = description;
		this.address = address;
		this.index = index;
		this.type = Type.values()[type];
		this.dhcpEnabled = dhcpEnabled;
		this.haveWins = haveWins;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	// returns adapter's hardware address
	public byte[] getAddress() {
		return address;
	}

	public int getIndex() {
		return index;
	}

	public Type getType() {
		return type;
	}

	public boolean isDhcpEnabled() {
		return dhcpEnabled;
	}

	public boolean isHaveWins() {
		return haveWins;
	}

	public static List<AdapterInfo> getAdapterInfos() {
		return nativeGetAdapterInfos();
	}

	private native static ArrayList<AdapterInfo> nativeGetAdapterInfos();

	@Override
	public String toString() {
		return "AdapterInfo [address="
				+ String.format("%02x:%02x:%02x:%02x:%02x:%02x", address[0], address[1], address[2], address[3],
						address[4], address[5]) + ", description=" + description + ", dhcpEnabled=" + dhcpEnabled
				+ ", haveWins=" + haveWins + ", index=" + index + ", name=" + name + ", type=" + type + "]";
	}

}
