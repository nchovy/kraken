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
package org.krakenapps.pcap.util;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import org.krakenapps.pcap.file.PcapFileInputStream;
import org.krakenapps.pcap.file.PcapFileOutputStream;
import org.krakenapps.pcap.packet.PcapPacket;

/**
 * Pcap utility for merge job.
 * 
 * @author mindori
 */
public class PcapMerger {
	private PcapMerger() {
	}

	/**
	 * Merge two pcap files.
	 * 
	 * @param to
	 *            the target pcap file. pcap data will be appended to this file.
	 * @param from
	 *            the source pcap file.
	 * @throws IOException
	 *             if there is no file, no access permission, or other io
	 *             related problems.
	 */
	public static void merge(File to, File from) throws IOException {
		PcapFileInputStream is = null;
		PcapFileOutputStream os = null;
		try {
			is = new PcapFileInputStream(from);
			os = new PcapFileOutputStream(to, is.getGlobalHeader());

			writePacket(is, os);
		} finally {
			closeInput(is);
			closeOutput(os);
		}
	}

	/**
	 * Merge file1 with file2, and write to output file. It doesn't hurt
	 * original pcap dump files.
	 * 
	 * @param output
	 *            the new pcap file. it will contain both file1 and file2.
	 * @param file1
	 *            the first pcap file. it will be written first.
	 * @param file2
	 *            the second pcap file. it will be written to output after first
	 *            pcap file.
	 * 
	 * @throws IOException
	 *             if there are no source files, have no read and/or write
	 *             permissions, or anything else.
	 */
	public static void merge(File output, File file1, File file2) throws IOException {
		PcapFileInputStream is1 = null;
		PcapFileInputStream is2 = null;
		PcapFileOutputStream os = null;
		try {

			is1 = new PcapFileInputStream(file1);
			is2 = new PcapFileInputStream(file2);
			os = new PcapFileOutputStream(output, is1.getGlobalHeader());

			writePacket(is1, os);
			writePacket(is2, os);
		} finally {
			closeInput(is1);
			closeInput(is2);
			closeOutput(os);
		}
	}

	private static void writePacket(PcapFileInputStream is, PcapFileOutputStream os) throws IOException {
		try {
			while (true) {
				PcapPacket packet = is.getPacket();
				if (packet == null)
					break;
				os.write(packet);
			}
		} catch (EOFException e) {
		}
	}

	private static void closeInput(PcapFileInputStream is) {
		if (is == null)
			return;
		try {
			is.close();
		} catch (Exception e) {
		}
	}

	private static void closeOutput(PcapFileOutputStream os) {
		if (os == null)
			return;
		try {
			os.close();
		} catch (Exception e) {
		}
	}

}
