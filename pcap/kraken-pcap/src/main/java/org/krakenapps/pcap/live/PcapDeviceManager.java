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
package org.krakenapps.pcap.live;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import org.krakenapps.pcap.routing.RoutingEntry;
import org.krakenapps.pcap.routing.RoutingTable;

public class PcapDeviceManager {
	private static final int DEFAULT_SNAPLEN = 65535;

	private static List<PcapDeviceMetadata> cachedDeviceMetadataList;
	private static final int MAX_NUMBER_OF_INSTANCE = 255;
	private static boolean[] allocatedHandles = new boolean[MAX_NUMBER_OF_INSTANCE];

	static {
		System.loadLibrary("kpcap");
		cachedDeviceMetadataList = Arrays.asList(getDeviceList());
	}

	public static PcapDeviceMetadata getDeviceMetadata(String name) {
		for (PcapDeviceMetadata device : getDeviceMetadataList()) {
			if (device.getName().equals(name))
				return device;
		}

		return null;
	}

	public static List<PcapDeviceMetadata> getDeviceMetadataList() {
		return cachedDeviceMetadataList;
	}

	private static native PcapDeviceMetadata[] getDeviceList();

	public static PcapDeviceMetadata getDeviceMetadata(InetAddress target) {
		if (target == null)
			throw new IllegalArgumentException("target should be not null");

		RoutingEntry entry = RoutingTable.findRoute(target);
		if (entry == null)
			throw new IllegalStateException("route not found for " + target.getHostAddress());

		return PcapDeviceManager.getDeviceMetadata(entry.getInterfaceName());
	}

	public static PcapDevice openFor(String target, int timeout) throws IOException {
		return openFor(InetAddress.getByName(target), timeout);
	}

	public static PcapDevice openFor(InetAddress target, int timeout) throws IOException {
		return openFor(target, Promiscuous.Off, timeout);
	}

	public static PcapDevice openFor(InetAddress target, Promiscuous promisc, int timeout) throws IOException {
		RoutingEntry entry = RoutingTable.findRoute(target);
		PcapDeviceMetadata metadata = PcapDeviceManager.getDeviceMetadata(entry.getInterfaceName());
		return open(metadata.getName(), promisc, timeout);
	}

	/**
	 * Opens the pcap device.
	 * 
	 * @param milliseconds
	 *            the timeout in milliseconds unit.
	 * @throws IOException
	 */
	public static PcapDevice open(String name, int milliseconds) throws IOException {
		return open(name, Promiscuous.Off, milliseconds);
	}

	/**
	 * Opens the pcap device.
	 * 
	 * @param promisc
	 *            the promiscuous mode or not
	 * @param milliseconds
	 *            the timeout in milliseconds unit
	 * @throws IOException
	 *             if max number of devices already opened.
	 */
	public static PcapDevice open(String name, Promiscuous promisc, int milliseconds) throws IOException {
		return open(name, promisc, milliseconds, DEFAULT_SNAPLEN);
	}

	public static PcapDevice open(String name, Promiscuous promisc, int milliseconds, int snaplen) throws IOException {
		if (name == null)
			throw new IllegalArgumentException("device name should not be null");

		int handle = -1;
		for (int i = 0; i < MAX_NUMBER_OF_INSTANCE; i++) {
			if (!allocatedHandles[i]) {
				handle = i;
				allocatedHandles[handle] = true;
				break;
			}
		}

		if (handle == -1)
			throw new IOException("Unable to open a device: " + MAX_NUMBER_OF_INSTANCE + " devices are already opened.");

		PcapDeviceMetadata info = getDeviceMetadata(name);
		if (info == null)
			throw new IOException("device not found: " + name);

		PcapDevice device = new PcapDevice(info, handle, name, snaplen, promisc == Promiscuous.On, milliseconds);
		device.addListener(new PcapDeviceEventListener() {
			@Override
			public void onClosed(PcapDevice device) {
				allocatedHandles[device.getHandle()] = false;
			}
		});

		return device;
	}
}
