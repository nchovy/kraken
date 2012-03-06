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
package org.krakenapps.pcap.file;

import org.krakenapps.pcap.util.ByteOrderConverter;

/**
 * GlobalHeader contains data about file format and capture configurations.
 * 
 * @see http://wiki.wireshark.org/Development/LibpcapFileFormat
 * @author mindori
 * @since 1.0
 */
public class GlobalHeader {
	/**
	 * 0xA1B2C3D4 or 0xD4C3B2A1
	 */
	private int magic;

	/**
	 * major version number
	 */
	private short major;

	/**
	 * minor version number
	 */
	private short minor;

	/**
	 * GMT to local correction
	 */
	private int tz;

	/**
	 * accuracy of timestamps
	 */
	private int sigfigs;

	/**
	 * max length of captured packets, in octets
	 */
	private int snaplen;

	/**
	 * data link type
	 */
	private int network;

	public GlobalHeader(int magic, short major, short minor, int tz, int sigfigs, int snaplen, int network) {
		this.magic = magic;
		this.major = major;
		this.minor = minor;
		this.tz = tz;
		this.sigfigs = sigfigs;
		this.snaplen = snaplen;
		this.network = network;
	}

	public int getMagicNumber() {
		return magic;
	}

	public short getMajorVersion() {
		return major;
	}

	public short getMinorVersion() {
		return minor;
	}

	public int getThiszone() {
		return tz;
	}

	public int getSigfigs() {
		return sigfigs;
	}

	public int getSnaplen() {
		return snaplen;
	}

	public int getNetwork() {
		return network;
	}

	public void swapByteOrder() {
		major = ByteOrderConverter.swap(major);
		minor = ByteOrderConverter.swap(minor);
		tz = ByteOrderConverter.swap(tz);
		sigfigs = ByteOrderConverter.swap(sigfigs);
		snaplen = ByteOrderConverter.swap(snaplen);
		network = ByteOrderConverter.swap(network);
	}

	@Override
	public String toString() {
		return String.format("version: %d.%d, timezone: %d, sigfigs: %d, snaplen: %d, network: %d", major, minor, tz,
				sigfigs, snaplen, network);
	}

}
