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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.krakenapps.pcap.PcapInputStream;
import org.krakenapps.pcap.packet.PacketHeader;
import org.krakenapps.pcap.packet.PcapPacket;
import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;
import org.krakenapps.pcap.util.ChainBuffer;

/**
 * PcapFileInputStream reads pcap packet stream from pcap dump file. At this
 * point of time, commonly used format is 2.4 version. It can read file
 * regardless of byte order because of the magic number of global header.
 * 
 * @author mindori
 * @see http://wiki.wireshark.org/Development/LibpcapFileFormat
 */
public class PcapFileInputStream implements PcapInputStream {
	private DataInputStream is;
	private GlobalHeader globalHeader;

	/**
	 * Opens pcap file input stream.
	 * 
	 * @param file
	 *            the file to be opened for reading
	 * @throws FileNotFoundException
	 *             if the file does not exist, is a directory rather than a
	 *             regular file, or for some other reason cannot be opened for
	 *             reading.
	 */
	public PcapFileInputStream(InputStream stream) throws IOException {
		is = new DataInputStream(stream);
		readGlobalHeader();
	}

	/**
	 * Opens pcap file input stream.
	 * 
	 * @param file
	 *            the file to be opened for reading
	 * @throws FileNotFoundException
	 *             if the file does not exist, is a directory rather than a
	 *             regular file, or for some other reason cannot be opened for
	 *             reading.
	 */
	public PcapFileInputStream(File file) throws IOException {
		is = new DataInputStream(new FileInputStream(file));
		readGlobalHeader();
	}

	/**
	 * Reads a packet from pcap file.
	 * 
	 * @exception EOFException
	 *                if this input stream reaches the end before reading four
	 *                bytes.
	 * @exception IOException
	 *                the stream has been closed and the contained input stream
	 *                does not support reading after close, or another I/O error
	 *                occurs.
	 */
	@Override
	public PcapPacket getPacket() throws IOException {
		return readPacket(globalHeader.getMagicNumber());
	}

	public GlobalHeader getGlobalHeader() {
		return globalHeader;
	}

	private void readGlobalHeader() throws IOException {
		int magic = is.readInt();
		short major = is.readShort();
		short minor = is.readShort();
		int tz = is.readInt();
		int sigfigs = is.readInt();
		int snaplen = is.readInt();
		int network = is.readInt();

		globalHeader = new GlobalHeader(magic, major, minor, tz, sigfigs, snaplen, network);

		if (globalHeader.getMagicNumber() == 0xD4C3B2A1)
			globalHeader.swapByteOrder();
	}

	private PcapPacket readPacket(int magicNumber) throws IOException, EOFException {
		PacketHeader packetHeader = readPacketHeader(magicNumber);
		Buffer packetData = readPacketData(packetHeader.getInclLen());
		return new PcapPacket(packetHeader, packetData);
	}

	private PacketHeader readPacketHeader(int magicNumber) throws IOException, EOFException {
		int tsSec = is.readInt();
		int tsUsec = is.readInt();
		int inclLen = is.readInt();
		int origLen = is.readInt();

		if (magicNumber == 0xD4C3B2A1) {
			tsSec = ByteOrderConverter.swap(tsSec);
			tsUsec = ByteOrderConverter.swap(tsUsec);
			inclLen = ByteOrderConverter.swap(inclLen);
			origLen = ByteOrderConverter.swap(origLen);
		}

		return new PacketHeader(tsSec, tsUsec, inclLen, origLen);
	}

	private Buffer readPacketData(int packetLength) throws IOException {
		byte[] packets = new byte[packetLength];
		is.read(packets);

		Buffer payload = new ChainBuffer();
		payload.addLast(packets);
		return payload;
		// return new PacketPayload(packets);
	}

	/**
	 * Closes pcap file handle.
	 */
	public void close() throws IOException {
		is.close();
	}
}
