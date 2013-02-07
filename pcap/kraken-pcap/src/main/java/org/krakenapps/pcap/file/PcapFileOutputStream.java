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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.List;

import org.krakenapps.pcap.PcapOutputStream;
import org.krakenapps.pcap.packet.PacketHeader;
import org.krakenapps.pcap.packet.PcapPacket;
import org.krakenapps.pcap.util.Buffer;

/**
 * PcapFileOutputStream writes pcap packet stream to pcap file.
 * 
 * @see http://wiki.wireshark.org/Development/LibpcapFileFormat
 * @author mindori
 * @since 1.1
 */
public class PcapFileOutputStream implements PcapOutputStream {
	private static final int MAX_CACHED_PACKET_NUMBER = 1000;
	private int cachedPacketNum = 0;

	private FileOutputStream fos;
	private List<Byte> list;

	public PcapFileOutputStream(File file) throws IOException {
		try {
			if (file.exists())
				throw new IOException("file exists: " + file.getName());
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		list = new ArrayList<Byte>();
		createGlobalHeader();
	}

	public PcapFileOutputStream(File file, GlobalHeader header) throws IOException {
		try {
			if (file.exists()) {
				fos = new FileOutputStream(file, true);
				list = new ArrayList<Byte>();
			}
			else {
				fos = new FileOutputStream(file);
				list = new ArrayList<Byte>();
				copyGlobalHeader(header);
			}
				
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void createGlobalHeader() {
		/* magic number(swapped) */
		list.add((byte) 0xd4);
		list.add((byte) 0xc3);
		list.add((byte) 0xb2);
		list.add((byte) 0xa1);

		/* major version number */
		list.add((byte) 0x02);
		list.add((byte) 0x00);

		/* minor version number */
		list.add((byte) 0x04);
		list.add((byte) 0x00);

		/* GMT to local correction */
		list.add((byte) 0x00);
		list.add((byte) 0x00);
		list.add((byte) 0x00);
		list.add((byte) 0x00);

		/* accuracy of timestamps */
		list.add((byte) 0x00);
		list.add((byte) 0x00);
		list.add((byte) 0x00);
		list.add((byte) 0x00);

		/* max length of captured packets, in octets */
		list.add((byte) 0xff);
		list.add((byte) 0xff);
		list.add((byte) 0x00);
		list.add((byte) 0x00);

		/* data link type(ethernet) */
		list.add((byte) 0x01);
		list.add((byte) 0x00);
		list.add((byte) 0x00);
		list.add((byte) 0x00);
	}
	
	private void copyGlobalHeader(GlobalHeader header) { 
		byte[] a = intToByteArray(header.getMagicNumber());
		byte[] b = shortToByteArray(header.getMajorVersion());
		byte[] c = shortToByteArray(header.getMinorVersion());
		byte[] d = intToByteArray(header.getThiszone());
		byte[] e = intToByteArray(header.getSigfigs());
		byte[] f = intToByteArray(header.getSnaplen());
		byte[] g = intToByteArray(header.getNetwork());
		
		list.add(a[0]);
		list.add(a[1]);
		list.add(a[2]);
		list.add(a[3]);
		
		list.add(b[1]);
		list.add(b[0]);
		
		list.add(c[1]);
		list.add(c[0]);
		
		list.add(d[3]);
		list.add(d[2]);
		list.add(d[1]);
		list.add(d[0]);
		
		list.add(e[3]);
		list.add(e[2]);
		list.add(e[1]);
		list.add(e[0]);
		
		list.add(f[3]);
		list.add(f[2]);
		list.add(f[1]);
		list.add(f[0]);
		
		list.add(g[3]);
		list.add(g[2]);
		list.add(g[1]);
		list.add(g[0]);
	}
	
	public void write(PcapPacket packet) throws IOException {
		PacketHeader packetHeader = packet.getPacketHeader();

		int tsSec = packetHeader.getTsSec();
		int tsUsec = packetHeader.getTsUsec();
		int inclLen = packetHeader.getInclLen();
		int origLen = packetHeader.getOrigLen();

		addInt(tsSec);
		addInt(tsUsec);
		addInt(inclLen);
		addInt(origLen);

		Buffer payload = packet.getPacketData();

		try {
			payload.mark();
			while (true) {
				list.add(payload.get());
			}
		} catch (BufferUnderflowException e) {
			payload.reset();
		}

		cachedPacketNum++;
		if (cachedPacketNum == MAX_CACHED_PACKET_NUMBER)
			flush();
	}

	private void addInt(int d) {
		list.add((byte) ((d & 0xff)));
		list.add((byte) ((d & 0xff00) >> 8));
		list.add((byte) ((d & 0xff0000) >> 16));
		list.add((byte) ((d & 0xff000000) >> 24));
	}

	private byte[] intToByteArray(int d) {
		return new byte[] { (byte) (d >>> 24), (byte) (d >>> 16), (byte) (d >>> 8), (byte) d };
	}

	private byte[] shortToByteArray(short s) {
		return new byte[] { (byte) (s >>> 8), (byte) s };
	}

	@Override
	public void flush() throws IOException {
		byte[] fileBinary = new byte[list.size()];
		for (int i = 0; i < fileBinary.length; i++) {
			fileBinary[i] = (byte) list.get(i);
		}

		list.clear();
		fos.write(fileBinary);
		cachedPacketNum = 0;
	}

	@Override
	public void close() throws IOException {
		flush();
		fos.close();
	}
}
