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
package org.krakenapps.linux.api;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.krakenapps.linux.api.WtmpEntry.Type;

public class Wtmp {

	public static List<WtmpEntry> getEntries() throws IOException {
		List<WtmpEntry> entries = new LinkedList<WtmpEntry>();
		RandomAccessFile raf = new RandomAccessFile(new File("/var/log/wtmp"), "r");
		try {
			while (true) {
				int type = swap(raf.readShort());
				raf.readShort(); // padding
				int pid = swap(raf.readInt());
				byte[] b = new byte[32];
				raf.read(b);
				byte[] id = new byte[4];
				raf.read(id);
				byte[] user = new byte[32];
				raf.read(user);
				byte[] host = new byte[256];
				raf.read(host);
				raf.readInt(); // skip exit_status

				int session = swap(raf.readInt());
				int seconds = swap(raf.readInt());
				swap(raf.readInt()); // microseconds

				Calendar c = Calendar.getInstance();
				c.set(Calendar.YEAR, 1970);
				c.set(Calendar.MONTH, 0);
				c.set(Calendar.DAY_OF_MONTH, 1);
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);
				c.add(Calendar.SECOND, seconds);
				c.add(Calendar.MILLISECOND, TimeZone.getDefault().getRawOffset());

				raf.read(new byte[36]); // addr + unused padding

				WtmpEntry entry = new WtmpEntry(Type.values()[type], c.getTime(), pid, parse(user), parse(host),
						session);
				entries.add(entry);
			}
		} catch (EOFException e) {
			// normal case
		} finally {
			raf.close();
		}

		return entries;
	}

	private static int swap(int v) {
		int a = v;
		int b = (a >> 24) & 0xFF;
		int c = (a >> 8) & 0xFF00;
		int d = (a << 8) & 0xFF0000;
		int e = (a << 24) & 0xFF000000;
		return (b | c | d | e);
	}

	public static short swap(short value) {
		short a = value;
		short b = (short) ((a >> 8) & 0xFF);
		short c = (short) ((a << 8) & 0xFF00);
		return (short) (b | c);
	}

	private static String parse(byte[] b) {
		int i = 0;
		while (b[i] != 0)
			i++;

		return new String(b, 0, i);
	}
}
