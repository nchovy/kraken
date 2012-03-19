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
package org.krakenapps.geoip.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.geoip.GeoIpLocation;
import org.krakenapps.geoip.GeoIpService;

@Component(name = "geoip-service")
@Provides
public class GeoIpServiceImpl implements GeoIpService {
	private static final int BLOCK_SIZE = 12;
	private static final int INDEX_ITEM_SIZE = 8;

	private static final String LOCATION_CSV = "geoip_locs.csv";
	private static final String GEOIP_LOCS_IDX = "geoip_locs.idx";
	private static final String GEOIP_BLOCKS_BIN = "geoip_blocks.bin";
	private static final File base = new File(System.getProperty("kraken.data.dir"), "kraken-geoip/");

	@Override
	public GeoIpLocation locate(InetAddress address) {
		RandomAccessFile raf = null;
		RandomAccessFile idx = null;
		RandomAccessFile csv = null;
		try {
			byte[] b = address.getAddress();
			long ip = (((long) (b[0] & 0xFF)) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | (b[3] & 0xFF);

			raf = new RandomAccessFile(new File(base, GEOIP_BLOCKS_BIN), "r");
			idx = new RandomAccessFile(new File(base, GEOIP_LOCS_IDX), "r");
			csv = new RandomAccessFile(new File(base, LOCATION_CSV), "r");

			int id = searchBlocks(raf, ip);
			if (id < 0)
				return null;

			long location = searchLocations(idx, id);
			if (location < 0)
				return null;

			csv.seek(location);
			return new GeoIpLocationImpl(csv.readLine());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			closeFile(raf);
			closeFile(csv);
			closeFile(idx);
		}
	}

	private void closeFile(RandomAccessFile raf) {
		if (raf == null)
			return;

		try {
			raf.close();
		} catch (IOException e) {
		}
	}

	private long searchLocations(RandomAccessFile raf, int locId) throws IOException {
		long low = 0;
		long high = raf.length() / INDEX_ITEM_SIZE - 1;

		while (low <= high) {
			long mid = low + (high - low) / 2;
			long pos = mid * INDEX_ITEM_SIZE;

			raf.seek(pos);
			int id = raf.readInt();

			if (locId > id)
				low = mid + 1;
			else if (locId < id)
				high = mid - 1;
			else {
				return raf.readInt();
			}
		}

		return -1;
	}

	private int searchBlocks(RandomAccessFile raf, long ip) throws IOException {
		long lowerBound = 0;
		long upperBound = raf.length() / BLOCK_SIZE - 1;

		while (lowerBound <= upperBound) {
			// bound value is item index
			long mid = lowerBound + (upperBound - lowerBound) / 2;
			long pos = mid * BLOCK_SIZE;

			raf.seek(pos);

			long begin = raf.readInt() & 0xFFFFFFFFL;
			long end = raf.readInt() & 0xFFFFFFFFL;
			long id = raf.readInt() & 0xFFFFFFFFL;

			if (begin <= ip && ip <= end) {
				return (int) id;
			} else if (ip < begin)
				upperBound = mid - 1;
			else if (end < ip)
				lowerBound = mid + 1;
		}

		return -1;
	}

	public void indexLocation(File f) throws IOException {
		int pos = 0;
		FileOutputStream bw = new FileOutputStream(new File(GEOIP_LOCS_IDX));
		RandomAccessFile raf = new RandomAccessFile(f, "r");

		byte[] all = new byte[(int) f.length()];
		raf.read(all);
		int mark = 0;

		ByteBuffer bb = ByteBuffer.allocate(INDEX_ITEM_SIZE);
		try {
			while (true) {
				int begin = mark;
				if (mark >= all.length)
					break;

				while (true) {
					if (mark >= all.length)
						break;

					if (all[mark++] == '\n')
						break;
				}

				int end = mark;

				int length = (int) (end - begin);
				String line = new String(all, begin, length);

				char c = line.charAt(0);
				if (c < '0' || c > '9') {
					pos += length;
					continue;
				}

				int id = Integer.parseInt(line.split(",")[0]);
				bb.clear();
				bb.putInt(id);
				bb.putInt(pos);
				bb.flip();
				bw.write(bb.array());

				pos += length;
			}
		} finally {
			raf.close();
			bw.close();
		}

	}

	@Override
	public void compileIpBlocks(File f) throws IOException {
		File output = new File(GEOIP_BLOCKS_BIN);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		FileOutputStream bw = new FileOutputStream(output);

		ByteBuffer bb = ByteBuffer.allocate(BLOCK_SIZE); // 4 + 4 + 4

		try {
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				if (line.charAt(0) != '"')
					continue;

				String[] tokens = line.replaceAll("\"", "").split(",");

				long begin = Long.parseLong(tokens[0]);
				long end = Long.parseLong(tokens[1]);
				long location = Long.parseLong(tokens[2]);

				bb.clear();
				bb.putInt((int) begin);
				bb.putInt((int) end);
				bb.putInt((int) location);
				bb.flip();

				bw.write(bb.array());
			}
		} finally {
			if (br != null)
				br.close();

			if (bw != null)
				bw.close();
		}
	}
}
