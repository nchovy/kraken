package org.krakenapps.logstorage.engine;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class LogFileHeader {
	public static final String MAGIC_STRING_DATA = "NCHOVY_BEAST_DAT";
	public static final String MAGIC_STRING_INDEX = "NCHOVY_BEAST_IDX";

	private String magicString;
	private short bom = (short) 0xFEFF;
	private short version;
	private short headerSize;
	private byte[] extraData;
	private static final short ALIGNED_HEADER_SIZE_BASE = 22;
	private static final short ALIGNED_HEADER_SIZE_POS = 20;

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

	public static LogFileHeader extractHeader(RandomAccessFile f) throws IOException, InvalidLogFileHeaderException {
		if (f.length() < ALIGNED_HEADER_SIZE_BASE) {
			throw new InvalidLogFileHeaderException("File size is too small.");
		}
		f.seek(ALIGNED_HEADER_SIZE_POS);
		short hdrSize = f.readShort();
		if (hdrSize > 65536) {
			throw new InvalidLogFileHeaderException("Invalid header size");
		}
		f.seek(0);
		byte[] hdr = new byte[hdrSize];
		f.readFully(hdr);
		return unserialize(hdr);
	}

	public static LogFileHeader extractHeader(BufferedRandomAccessFileReader f) throws IOException,
			InvalidLogFileHeaderException {
		if (f.length() < ALIGNED_HEADER_SIZE_BASE) {
			throw new InvalidLogFileHeaderException("File size is too small.");
		}
		f.seek(ALIGNED_HEADER_SIZE_POS);
		short hdrSize = f.readShort();
		if (hdrSize > 65536) {
			throw new InvalidLogFileHeaderException("Invalid header size");
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