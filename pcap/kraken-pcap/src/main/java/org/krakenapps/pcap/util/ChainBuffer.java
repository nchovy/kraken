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

import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.InvalidMarkException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author mindori
 */
public class ChainBuffer implements Buffer {
	private List<byte[]> buffers;

	/* start[0] = bufIndex of start point. baseOffset = offset of start point. */
	private int baseIndex;
	private int baseOffset;

	private int markIndex = -1;
	private int markOffset = -1;

	private int bufIndex = 0;
	private int bufOffset = 0;

	public ChainBuffer() {
		buffers = new ArrayList<byte[]>();

		baseIndex = 0;
		baseOffset = 0;
	}

	public ChainBuffer(byte[] b) {
		this();
		addLast(b);
	}

	/* copy constructor */
	public ChainBuffer(Buffer other) {
		buffers = new ArrayList<byte[]>();
		buffers.addAll(other.getBuffers());

		int[] metaData = other.getMetaData();

		baseIndex = metaData[0];
		baseOffset = metaData[1];

		markIndex = metaData[2];
		markOffset = metaData[3];

		bufIndex = metaData[4];
		bufOffset = metaData[5];
	}

	/* called by copy constructor */
	@Override
	public int[] getMetaData() {
		return new int[] { baseIndex, baseOffset, markIndex, markOffset, bufIndex, bufOffset };
	}

	@Override
	public List<byte[]> getBuffers() {
		return buffers;
	}

	@Override
	public int getCapacity() {
		int capacity = 0;
		for (byte[] b : buffers)
			capacity += b.length;
		return capacity;
	}

	@Override
	public int getBaseIndex() {
		return baseIndex;
	}

	@Override
	public int getBaseOffset() {
		return baseOffset;
	}

	@Override
	public int getBufIndex() {
		return bufIndex;
	}

	@Override
	public int getOffset() {
		return bufOffset;
	}

	@Override
	public int position() {
		int absPos = 0;
		if (bufIndex > 0) {
			int loopCount = 0;
			for (byte[] buffer : buffers) {
				absPos += buffer.length;
				if (loopCount >= (bufIndex - 1))
					break;
				loopCount++;
			}
		}
		absPos += bufOffset;
		return absPos;
	}

	@Override
	public Buffer position(int newPosition) {
		int absPos = 0;
		int i = 0;

		if (newPosition < 0)
			throw new IllegalArgumentException();

		for (byte[] buffer : buffers) {
			if (absPos + buffer.length >= newPosition) {
				bufIndex = i;
				bufOffset = newPosition - absPos;
				return this;
			}
			absPos += buffer.length;
			i++;
		}
		throw new IllegalArgumentException();
	}

	@Override
	public void addFirst(byte[] buffer) {
		if (buffer == null)
			return;

		buffers.add(0, buffer);
	}

	@Override
	public void addLast(byte[] buffer) {
		if (buffer == null)
			return;

		buffers.add(buffer);
	}

	@Override
	public void addFirst(Buffer buffer) {
		if (buffer == null)
			return;

		List<byte[]> newBufList = buffer.getBuffers();
		buffers.addAll(0, newBufList);
	}

	@Override
	public void addLast(Buffer buffer) {
		if (buffer == null)
			return;

		/* copy to current offset ~ EOB */
		List<byte[]> l = buffer.getBuffers();
		int i = buffer.getBaseIndex();
		int j = buffer.getBaseOffset();

		if (i >= l.size())
			return;

		byte[] b = l.get(i);
		if (j > 0) {
			byte[] t = new byte[b.length - j];
			for (int k = 0; k < t.length; k++) {
				t[k] = b[k + j];
			}
			buffers.add(t);
			if (i + 1 < l.size())
				buffers.addAll(l.subList(i + 1, l.size()));
		} else {
			if (i < l.size())
				buffers.addAll(l.subList(i, l.size()));
		}
	}

	@Override
	public void addLast(Buffer buffer, int length) {
		if (buffer == null)
			return;

		List<byte[]> l = buffer.getBuffers();
		int i = buffer.getBufIndex();
		int j = buffer.getOffset();
		if (i >= l.size() || length <= 0)
			return;
		/* calculate bufIndex */
		buffer.mark();
		if (buffer.skip(length) == null)
			return;
		int m = buffer.getBufIndex();
		int n = buffer.getOffset();
		buffer.reset();

		byte[] b = l.get(i);
		if (j > 0) {
			if (m == i) {
				byte[] t = new byte[n - j];
				for (int k = 0; k < t.length; k++) {
					t[k] = b[k + j];
				}
				buffers.add(t);
			}

			else {
				if (m == i + 1) {
					byte[] t = new byte[b.length - j];
					for (int k = 0; k < t.length; k++) {
						t[k] = b[k + j];
					}
					buffers.add(t);

					if (n > 0) {
						byte[] b1 = l.get(m);
						byte[] t1 = new byte[n];
						for (int k = 0; k < t1.length; k++) {
							t1[k] = b1[k];
						}
						buffers.add(t1);
					}
				} else {
					byte[] t = new byte[b.length - j];
					for (int k = 0; k < t.length; k++) {
						t[k] = b[k + j];
					}
					buffers.add(t);

					if (i + 1 == m - 1)
						buffers.add(l.get(i + 1));
					else
						buffers.addAll(l.subList(i + 1, m));

					if (n > 0) {
						byte[] b1 = l.get(m);
						byte[] t1 = new byte[n];
						for (int k = 0; k < t1.length; k++) {
							t1[k] = b1[k];
						}
						buffers.add(t1);
					}
				}
			}
		} else {
			/* j == 0 */
			if (m == i) {
				byte[] t1 = new byte[n];
				for (int k = 0; k < t1.length; k++) {
					t1[k] = b[k];
				}
				buffers.add(t1);
			}

			else {
				if (i == m - 1) {
					buffers.add(b);
					if (n > 0) {
						byte[] b1 = l.get(m);
						byte[] t1 = new byte[n];
						for (int k = 0; k < t1.length; k++) {
							t1[k] = b1[k];
						}
						buffers.add(t1);
					}
				} else {
					buffers.addAll(l.subList(i, m));
					if (n > 0) {
						byte[] b1 = l.get(m);
						byte[] t1 = new byte[n];
						for (int k = 0; k < t1.length; k++) {
							t1[k] = b1[k];
						}
						buffers.add(t1);
					}
				}
			}
		}
	}

	@Override
	public Buffer skip(int pos) {
		/* move failed => don't moved. */
		if (pos <= 0)
			return null;

		int skipped = 0;

		int i = bufIndex;
		byte[] buf = buffers.get(i);
		int next = buf.length - bufOffset;

		skipped += next;
		if (skipped >= pos) {
			bufOffset += pos;
			if (bufOffset >= buf.length) {
				bufIndex += 1;
				bufOffset = 0;
			}
			// discardReadBytes();
			return this;
		}
		i++;

		while (i < buffers.size()) {
			next = buffers.get(i).length;
			skipped += next;
			if (skipped >= pos) {
				int remain = skipped - pos;
				bufIndex = i;
				bufOffset = (next - remain);

				if (bufOffset >= next) {
					bufIndex += 1;
					bufOffset = 0;
				}
				// discardReadBytes();
				return this;
			}
			i++;
		}
		return null;
	}

	@Override
	public byte get() throws BufferUnderflowException {
		/* fetch address: [bufIndex, offset] */
		if (bufIndex >= buffers.size())
			throw new BufferUnderflowException();
		byte[] buf = buffers.get(bufIndex);

		if (bufOffset >= buf.length) {
			bufOffset = 0;
			bufIndex += 1;
			if (bufIndex >= buffers.size())
				throw new BufferUnderflowException();
			buf = buffers.get(bufIndex);
		}

		byte retVal = buf[bufOffset];

		/* modify offset */
		if ((bufOffset + 1) >= buf.length) {
			bufIndex += 1;
			bufOffset = 0;
		} else
			bufOffset += 1;
		return retVal;
	}

	@Override
	public short getShort() throws BufferUnderflowException {
		try {
			byte[] b = new byte[2];
			gets(b, 0, 2);

			short s = 0;
			for (int i = 0; i < 2; i++) {
				s <<= 8;
				s ^= (long) b[i] & 0xFF;
			}

			return s;
		} catch (BufferUnderflowException e) {
			throw e;
		}
	}

	@Override
	public int getUnsignedShort() {
		return getShort() & 0xFFFF;
	}

	@Override
	public int getInt() throws BufferUnderflowException {
		try {
			byte[] b = new byte[4];
			gets(b, 0, 4);

			int s = 0;
			for (int i = 0; i < 4; i++) {
				s <<= 8;
				s ^= (long) b[i] & 0xFF;
			}

			return s;
		} catch (BufferUnderflowException e) {
			throw e;
		}
	}

	@Override
	public long getUnsignedInt() {
		return getInt() & 0xFFFFFFFFl;
	}

	@Override
	public long getLong() throws BufferUnderflowException {
		try {
			byte[] b = new byte[8];
			gets(b, 0, 8);

			long s = 0;
			for (int i = 0; i < 8; i++) {
				s <<= 8;
				s ^= (long) b[i] & 0xFFl;
			}

			return s;
		} catch (BufferUnderflowException e) {
			throw e;
		}
	}

	@Override
	public String getString(int length) throws BufferUnderflowException {
		byte[] str = new byte[length];
		try {
			gets(str, 0, length);
			return new String(str);
		} catch (BufferUnderflowException e) {
			throw e;
		}
	}

	@Override
	public String getString(int length, String charsetName) throws BufferUnderflowException {
		byte[] str = new byte[length];
		try {
			gets(str, 0, length);
			return new String(str, charsetName);
		} catch (BufferUnderflowException e) {
			throw e;
		} catch (UnsupportedEncodingException e) {
			return new String(str);
		}
	}

	@Override
	public String getString(int length, Charset charset) throws BufferUnderflowException {
		byte[] str = new byte[length];
		try {
			gets(str, 0, length);
			return new String(str, charset);
		} catch (BufferUnderflowException e) {
			throw e;
		}
	}

	@Override
	public void gets(byte[] buffer) {
		gets(buffer, 0, buffer.length);
	}

	@Override
	public void gets(byte[] buffer, int offset, int length) throws BufferUnderflowException {
		if (length == 0)
			return;

		if (bufIndex >= buffers.size())
			throw new BufferUnderflowException();

		/* calculate length */
		int remain = readableBytes();

		if (remain < length)
			throw new BufferUnderflowException();

		int index = offset;

		int i = 0;
		int j = bufIndex;
		int bufI = bufIndex;
		int off = bufOffset;

		int bufCount = 0;
		int count = 0;

		for (byte[] b : buffers) {
			if (bufCount < j) {
				/* skip buffer until count == bufIndex */
				bufCount++;
				continue;
			}

			for (i = off; i < b.length; i++) {
				if (count == length) {
					off = i;
					break;
				}
				buffer[index] = b[i];
				index++;
				count++;
			}

			if (count == length) {
				if (i == b.length) {
					bufI++;
					off = 0;
				}
				break;
			} else {
				/* go to next buffer */
				bufI++;
				off = 0;
			}
		}
		bufIndex = bufI;
		bufOffset = off;
	}

	@Override
	public int bytesBefore(byte[] target) {
		/* if, stateNum equals to target.length => pattern matching to target. */
		mark();

		int stateNum = 0;
		int length = 0;

		if (bufIndex >= buffers.size()) {
			reset();
			return 0;
		}

		/* retrieve first byte array */
		byte[] bs = buffers.get(bufIndex);

		for (int i = bufOffset; i < bs.length; i++) {
			if (bs[i] == target[stateNum])
				stateNum++;
			else if (bs[i] == target[0]) {
				stateNum = 1;
			} else
				stateNum = 0;
			length++;

			if (stateNum == target.length) {
				/* return length */
				reset();
				return (length - target.length);
			}
		}

		/* get sublist from bufList */
		int index = bufIndex + 1;
		List<byte[]> subList = buffers.subList(index, buffers.size());
		for (byte[] bs2 : subList) {
			for (byte b : bs2) {
				if (b == target[stateNum])
					stateNum++;
				else if (b == target[0])
					stateNum = 1;
				else
					stateNum = 0;
				length++;

				if (stateNum == target.length) {
					/* return length */
					reset();
					return (length - target.length);
				}
			}
		}
		reset();
		return 0;
	}

	@Override
	public void mark() {
		markIndex = bufIndex;
		markOffset = bufOffset;
	}

	@Override
	public void rewind() {
		bufIndex = baseIndex;
		bufOffset = baseOffset;

		markIndex = -1;
		markOffset = -1;
	}

	@Override
	public Buffer reset() {
		if (markIndex == -1 && markOffset == -1)
			throw new InvalidMarkException();

		if (bufIndex > markIndex) {
			int rewindOffset = bufOffset;
			for (int i = bufIndex - 1; i > markIndex; i--) {
				byte[] buf = buffers.get(i);
				rewindOffset += buf.length;
			}
			byte[] markBuf = buffers.get(markIndex);
			rewindOffset += (markBuf.length - markOffset);
			return reset(rewindOffset);
		} else if (bufIndex < markIndex) {
			int absPos = 0;
			int i = bufIndex + 1;
			while (i < markIndex) {
				absPos += buffers.get(i).length;
				i++;
			}
			absPos += buffers.get(bufIndex).length - bufOffset;
			absPos += markOffset;

			return skip(absPos);
		} else {
			/* markIndex = bufIndex */
			if (bufOffset > markOffset) {
				return reset(bufOffset - markOffset);
			} else {
				return skip(markOffset - bufOffset);
			}
		}
	}

	public Buffer reset(int rewindOffset) {
		if (bufIndex > baseIndex) {
			if (bufOffset >= rewindOffset) {
				bufOffset -= rewindOffset;
			} else {
				int i = bufIndex;
				int sumOffset = bufOffset;
				byte[] b;

				do {
					i--;
					b = buffers.get(i);
					sumOffset += b.length;
				} while (sumOffset < rewindOffset && i > 0);

				if (sumOffset < rewindOffset) {
					/* retrieve failed: bufList[bufIndex..0] */
					return null;
				} else {
					bufIndex = i;
					bufOffset = sumOffset - rewindOffset;
				}
			}
		} else {
			/* bufIndex = baseIndex */
			if (bufOffset >= rewindOffset)
				bufOffset -= rewindOffset;
			else
				/* Invalid rewindOffset */
				return null;
		}
		return this;
	}

	public void discardReadBytes() {
		/* Truncated start ~ current */
		if (bufIndex >= buffers.size())
			return;
		byte[] buf = buffers.get(bufIndex);
		if (bufOffset >= buf.length) {
			bufIndex += 1;
			bufOffset = 0;
		}
		
		baseIndex = bufIndex;
		baseOffset = bufOffset;
	}

	@Override
	public int readableBytes() {
		if (buffers.size() <= 0 || isEOB())
			return 0;

		byte[] buf = buffers.get(bufIndex);
		int remain = buf.length - bufOffset;

		for (int i = bufIndex + 1; i < buffers.size(); i++) {
			buf = buffers.get(i);
			remain += buf.length;
		}

		return remain;
	}

	@Override
	public Buffer clear() {
		baseIndex = 0;
		baseOffset = 0;

		bufIndex = 0;
		bufOffset = 0;

		markIndex = -1;
		markOffset = -1;

		return this;
	}

	@Override
	public boolean isEOB() {
		if (bufIndex >= buffers.size()) {
			return true;
		}
		return false;
	}

	@Override
	public Buffer duplicate() {
		// TODO
		return null;
	}

	@Override
	public Buffer flip() {
		int i = bufIndex + 1;
		while(i < buffers.size()) {
			buffers.remove(i);
		} 

		if(bufIndex >= buffers.size()) {
			bufIndex = baseIndex;
			bufOffset = baseOffset;

			markIndex = -1;
			markOffset = -1;
			return this;
		}
		
		byte[] b = buffers.get(bufIndex);
		byte[] newb = Arrays.copyOf(b, bufOffset);
		buffers.remove(bufIndex);
		buffers.add(bufIndex, newb);

		bufIndex = baseIndex;
		bufOffset = baseOffset;

		markIndex = -1;
		markOffset = -1;
		return this;
	}
}