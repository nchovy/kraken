/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.logstorage.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class LogFileHeader {
	public static final String MAGIC_STRING_DATA = "NCHOVY_BEAST_DAT";
	public static final String MAGIC_STRING_INDEX = "NCHOVY_BEAST_IDX";
	public static final short ALIGNED_HEADER_SIZE_BASE = 22;
	public static final short ALIGNED_HEADER_SIZE_POS = 20;

	private String magicString;
	private short bom = (short) 0xFEFF;
	private short version;
	private short headerSize;
	private byte[] extraData;

	public LogFileHeader(short version, String magicString) {
		this.version = version;
		this.magicString = magicString;
		if (magicString.length() != 16) {
			throw new IllegalStateException();
		}
		updateHeaderSize();
	}

	public short version() {
		return version;
	}

	public int size() {
		return headerSize;
	}

	public byte[] getExtraData() {
		return extraData;
	}

	public void setExtraData(byte[] e) {
		extraData = Arrays.copyOf(e, e.length);
		updateHeaderSize();
	}

	private int getAlignedHeaderSize() {
		int extraDataLength = 0;
		if (extraData != null)
			extraDataLength = extraData.length;
		return (ALIGNED_HEADER_SIZE_BASE + extraDataLength - 1 + 4) / 4 * 4;
	}

	private void updateHeaderSize() {
		headerSize = (short) getAlignedHeaderSize();
	}

	public byte[] serialize() {
		ByteBuffer buf = ByteBuffer.allocate(getAlignedHeaderSize());
		try {
			buf.put(magicString.getBytes("Latin1"), 0, 16);
			buf.putShort((short) bom);
			buf.putShort(version);
			int hdrSizePos = buf.position();
			// XXX
			if (hdrSizePos != 20)
				throw new IllegalStateException();
			buf.putShort((short) getAlignedHeaderSize()); // headerSize
			if (extraData != null)
				buf.put(extraData);
			int headerSize = buf.position();
			// XXX
			if (headerSize > getAlignedHeaderSize()) {
				throw new IllegalStateException();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return Arrays.copyOfRange(buf.array(), 0, getAlignedHeaderSize());
	}

	public static LogFileHeader extractHeader(RandomAccessFile f, File path) throws IOException, InvalidLogFileHeaderException {
		if (f.length() < ALIGNED_HEADER_SIZE_BASE) {
			throw new InvalidLogFileHeaderException("File size is too small: " + path.getAbsolutePath());
		}
		f.seek(ALIGNED_HEADER_SIZE_POS);
		short hdrSize = f.readShort();
		if (hdrSize > 65536) {
			throw new InvalidLogFileHeaderException("Invalid header size: " + path.getAbsolutePath());
		}
		f.seek(0);
		byte[] hdr = new byte[hdrSize];
		f.readFully(hdr);
		return unserialize(hdr);
	}

	public static LogFileHeader extractHeader(BufferedRandomAccessFileReader f, File path) throws IOException,
			InvalidLogFileHeaderException {
		if (f.length() < ALIGNED_HEADER_SIZE_BASE) {
			throw new InvalidLogFileHeaderException("File size is too small: " + path.getAbsolutePath());
		}
		f.seek(ALIGNED_HEADER_SIZE_POS);
		short hdrSize = f.readShort();
		if (hdrSize > 65536) {
			throw new InvalidLogFileHeaderException("Invalid header size: " + path.getAbsolutePath());
		}
		f.seek(0);
		byte[] hdr = new byte[hdrSize];
		f.readFully(hdr);
		return unserialize(hdr);
	}

	public static LogFileHeader unserialize(byte[] array) throws InvalidLogFileHeaderException {
		try {
			ByteBuffer buf = ByteBuffer.wrap(array);
			byte[] magicStringBuf = new byte[16];
			buf.get(magicStringBuf);
			String magicString = new String(magicStringBuf, Charset.forName("Latin1"));
			short bom = buf.getShort();
			short version = buf.getShort();
			short headerSize = buf.getShort();
			byte[] extraData = null;
			if (headerSize != buf.position())
				extraData = Arrays.copyOfRange(buf.array(), buf.position(), headerSize);

			LogFileHeader hdr = new LogFileHeader(version, magicString);
			hdr.bom = bom;
			hdr.version = version;
			hdr.headerSize = headerSize;
			hdr.extraData = extraData;

			validate(hdr);

			return hdr;
		} catch (Exception e) {
			throw new InvalidLogFileHeaderException(e);
		}
	}

	private static void validate(LogFileHeader hdr) throws InvalidLogFileHeaderException {
		if (!MAGIC_STRING_DATA.equals(hdr.magicString) && !MAGIC_STRING_INDEX.equals(hdr.magicString))
			throw new InvalidLogFileHeaderException("File starts with invalid magic string.");
	}
}