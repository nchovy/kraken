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

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author mindori
 */
public interface Buffer {
	/**
	 * Returns all internal chained byte buffers.
	 * 
	 * @return the byte buffer list.
	 */
	List<byte[]> getBuffers();

	int[] getMetaData();

	int getCapacity();

	int getBaseIndex();

	int getBaseOffset();

	int getBufIndex();

	int getOffset();

	int position();

	Buffer position(int newPosition);

	/**
	 * Add all target's internal buffers to head of internal buffer list.
	 * 
	 * @param buffer
	 *            the buffer that you want to add to head of internal buffers.
	 */
	void addFirst(Buffer buffer);

	/**
	 * Add all target's inernal buffers to end of internal buffer list.
	 * 
	 * @param buffer
	 *            the buffer that you want to add to end of internal buffers.
	 */
	void addLast(Buffer buffer);

	void addLast(Buffer buffer, int length);

	/**
	 * Add a byte buffer to head of internal buffer list.
	 * 
	 * @param buffer
	 *            the byte buffer that you want to add to head of internal buffers.
	 */
	void addFirst(byte[] buffer);

	/**
	 * Add a byte buffer to end of internal buffer list.
	 * 
	 * @param buffer
	 *            the byte buffer that you want to add to end of internal buffers.
	 */
	void addLast(byte[] buffer);

	Buffer skip(int pos);

	/**
	 * Get a byte from current position. Position will be increased.
	 * 
	 * @return a byte
	 * @throws BufferUnderflowException
	 *             if position met end of buffers.
	 */

	byte get();

	/**
	 * Get short from current position. Position will be increased.
	 * 
	 * @return short data
	 * @throws BufferUnderflowException
	 *             if position met end of buffers.
	 */
	short getShort();

	int getUnsignedShort();

	/**
	 * Get integer from current position. Position will be increased.
	 * 
	 * @return integer data
	 * @throws BufferUnderflowException
	 *             if position met end of buffers.
	 */
	int getInt();

	long getUnsignedInt();

	/**
	 * Get long from current position. Position will be increased.
	 * 
	 * @return long data
	 * @throws BufferUnderflowException
	 *             if position met end of buffers.
	 */
	long getLong();

	/**
	 * Get string from current position. Position will be increased.
	 * 
	 * @return string data
	 * @throws BufferUnderflowException
	 *             if position met end of buffers.
	 */
	String getString(int length);

	/**
	 * Get string from current position. Position will be increased.
	 * 
	 * @param length
	 *            the string length that you want to read.
	 * @param charsetName
	 *            the charset name for decoding.
	 * @return string data
	 * @throws BufferUnderflowException
	 *             if position met end of buffers.
	 */
	String getString(int length, String charsetName);

	/**
	 * Get string from current position. Position will be increased.
	 * 
	 * @param length
	 *            the string length that you want to read.
	 * @param charset
	 *            the charset for decoding.
	 * @return string data
	 * @throws BufferUnderflowException
	 *             if position met end of buffers.
	 */
	String getString(int length, Charset charset);

	/**
	 * Get byte array from current position.
	 * 
	 * @param buffer
	 *            the output buffer
	 */
	void gets(byte[] buffer);

	/**
	 * Get byte array from current position.
	 * 
	 * @param buffer
	 *            the output buffer
	 * @param offset
	 *            the index of the first byte to fill
	 * @param length
	 *            the number of bytes to read
	 */
	void gets(byte[] buffer, int offset, int length);

	/**
	 * Mark current position. You can back to marked position using rewind call.
	 */
	void mark();

	/**
	 * Discard already read data. It will move base position.
	 */
	void discardReadBytes();

	/**
	 * Move current position to base position and the mark is discarded.
	 */
	void rewind();

	/**
	 * Move current position to marked position.
	 * 
	 * @return
	 */
	Buffer reset();

	/**
	 * Find the first occurrence offset of the target byte pattern from current position.
	 * 
	 * @param target
	 *            the byte pattern that you want to search for.
	 * @return the offset from current position, first offset of the target pattern. for example, if you call bytesBefore('\r\n') for 'hello\r\n', it will return 5.
	 */
	int bytesBefore(byte[] target);

	/**
	 * Returns the number of bytes between the current position and the limit.
	 * 
	 * @return the number of bytes remaining in this buffer.
	 */
	int readableBytes();

	/**
	 * Clears this buffer. The position is set to zero and base position is set to zero too. and mark is discarded.
	 */
	Buffer clear();

	/**
	 * Returns true if current position is after the last byte.
	 * 
	 * @return True if current position is after the last byte
	 */
	boolean isEOB();

	Buffer duplicate();

	/**
	 * Flips this buffer. The position is set to zero. If the mark is defined then it is discarded.
	 */
	Buffer flip();
}