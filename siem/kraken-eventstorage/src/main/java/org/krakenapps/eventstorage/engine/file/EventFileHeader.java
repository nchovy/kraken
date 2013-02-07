/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.eventstorage.engine.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class EventFileHeader {
	public static final String MAGIC_STRING_INDEX = "KRAKEN_EVENT_IDX";
	public static final String MAGIC_STRING_POINTER = "KRAKEN_EVENT_PTR";
	public static final String MAGIC_STRING_DATA = "KRAKEN_EVENT_DAT";

	private static final String STRING_ENCODING = "Latin1";
	private static final short ALIGNED_HEADER_SIZE_BASE = 22;
	private static final short ALIGNED_HEADER_SIZE_POS = 20;

	private String magicString;
	private short bom = (short) 0xFEFF;
	private short version;
	private short headerSize;
	private byte[] extraData;

	private EventFileHeader() {
	}

	public EventFileHeader(short version, String magicString) {
		this.version = version;
		this.magicString = magicString;
		this.headerSize = getAlignedHeaderSize();
		magicStringValidate(this);
	}

	public static EventFileHeader extractHeader(File f) throws IOException {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(f, "r");
			if (f.length() < ALIGNED_HEADER_SIZE_BASE)
				throw new IllegalArgumentException("File size is too small: " + f.getAbsolutePath());

			raf.seek(ALIGNED_HEADER_SIZE_POS);
			short hdrSize = raf.readShort();
			if (hdrSize > Short.MAX_VALUE)
				throw new IllegalArgumentException("Invalid header size: " + f.getAbsolutePath());

			raf.seek(0);
			byte[] hdr = new byte[hdrSize];
			raf.readFully(hdr);

			return unserialize(hdr);
		} finally {
			if (raf != null)
				raf.close();
		}
	}

	public static EventFileHeader unserialize(byte[] array) {
		try {
			EventFileHeader hdr = new EventFileHeader();
			ByteBuffer buf = ByteBuffer.wrap(array);
			byte[] magic = new byte[16];
			buf.get(magic);
			hdr.magicString = new String(magic, STRING_ENCODING);
			hdr.bom = buf.getShort();
			hdr.version = buf.getShort();
			hdr.headerSize = buf.getShort();
			if (hdr.headerSize != buf.position())
				hdr.extraData = Arrays.copyOfRange(buf.array(), buf.position(), hdr.headerSize);
			magicStringValidate(hdr);

			return hdr;
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	private static void magicStringValidate(EventFileHeader hdr) {
		if (!MAGIC_STRING_INDEX.equals(hdr.magicString) && !MAGIC_STRING_POINTER.equals(hdr.magicString)
				&& !MAGIC_STRING_DATA.equals(hdr.magicString)) {
			throw new IllegalArgumentException();
		}
	}

	public String magicString() {
		return magicString;
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
		this.extraData = Arrays.copyOf(e, e.length);
		this.headerSize = getAlignedHeaderSize();
	}

	public byte[] serialize() {
		ByteBuffer buf = ByteBuffer.allocate(getAlignedHeaderSize());
		try {
			buf.put(magicString.getBytes(STRING_ENCODING));
			buf.putShort(bom);
			buf.putShort(version);
			if (buf.position() != 20)
				throw new IllegalStateException();

			buf.putShort(getAlignedHeaderSize());
			if (extraData != null)
				buf.put(extraData);
		} catch (UnsupportedEncodingException e) {
		}

		return buf.array();
	}

	private short getAlignedHeaderSize() {
		int length = ALIGNED_HEADER_SIZE_BASE + ((extraData != null) ? extraData.length : 0);
		return (short) ((length + 3) / 4 * 4);
	}
}
