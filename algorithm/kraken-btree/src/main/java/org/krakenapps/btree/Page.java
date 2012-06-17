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

/*
 * PAGE_HEADER: flag(2), count(2), left(4), right(4), right-child(4), upper(4), 
 * 	            first_freeblock (2), first_record_pos(2), number_of_fragmented_freebytes(2), 
 * +---------------+---------+---------------+-----------------+
 * |PAGE_HEADER(26)|Slots(8n)| unalloc space | Records         |
 * +---------------+---------+---------------+-----------------+
 * 
 * Slots and Records are managed in sorted order.
 * 
 * Record:
 * +---------------------+----------------------------------+-----+-------+
 * | data length(varlen) | key length or key itself(varlen) | Key | Value |
 * +---------------------+----------------------------------+-----+-------+
 * |<-------------------------- Record Length --------------------------->|
 *  
 * if (record.keyLength > page.size * page.maxPayloadFraction) 
 * 		*record has overflow page*
 * 
 * Record which has overflow page:
 * +---------------------+----------------------------------+-----+-------+-------------------------+
 * | data length(varlen) | key length or key itself(varlen) | Key | Value | overflow page number(4) |
 * +---------------------+----------------------------------+-----+-------+-------------------------+
 * |<-------------------------- Record Length ----------------------------------------------------->|
 *
 * Record Length := varlen + varlen + data length + key length      : key length <= page.size * page.maxPayloadFraction
 *               or varlen + varlen + data length + key length + 4  : key length >  page.size * page.maxPayloadFraction
 *
 * Slot:
 * +-----------+------------------+
 * | Offset(2) | Record Length(2) |
 * +-----------+------------------+
 * 
 * free block header
 * +-------------------+-------------------------------------------+
 * | next block pos(2) | length of this block(2) (excludes header) | 
 * +-------------------+-------------------------------------------+ 
 * 
 */

package org.krakenapps.btree;

import java.nio.ByteBuffer;

public class Page {
	// (flag, count), left, right, right-child, upper
	// (first_freeblock, first_recore_pos), (number_of_fragmented_freebytes, )
	public static final int PAGE_HEADER_SIZE = 26;
	public static final int SLOT_SIZE = 4;

	// record = record header + key + value
	// record header: key length(2) + overflow(1) + non-unique(1)
	//
	@Deprecated
	public static final int RECORD_HEADER_SIZE = 4;

	private boolean dirty;
	private int number;
	private Schema schema;

	private ByteBuffer bb;
	private byte[] data;

	public Page(Schema schema) {
		this(0, schema, null);
	}

	public Page(int number, Schema schema, byte[] data) {
		this.number = number;
		this.schema = schema;

		if (data != null)
			this.data = data;
		else
			this.data = new byte[schema.getPageSize()];

		this.bb = ByteBuffer.wrap(this.data);
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clearDirty() {
		this.dirty = false;
	}

	public int getNumber() {
		return number;
	}

	public Schema getSchema() {
		return schema;
	}

	public byte[] getData() {
		return data;
	}

	public boolean insert(RowKey key, RowEntry value) {
		byte[] valueBytes = value.getBytes();
		int keyLength = key.getBytes().length;
		int valueLength = valueBytes.length;

		int recordLength = getRecordHeaderSize(keyLength, valueLength) + keyLength + valueLength;
		if (!checkFreeSpace(recordLength))
			return false;

		int before = findSlotBefore(key);
		int slot = before + 1;

		// calculate new offset
		int offset = 0;
		if (before >= 0) {
			int pos = getSlotPosition(before);
			int beforeOffset = bb.getShort(pos) & 0xFFFF;
			int beforeLength = bb.getShort(pos + 2) & 0xFFFF;
			offset = beforeOffset + beforeLength;
		}

		// move existing data
		// order: last to first
		// move record one by one
		// record is already ordered by key.
		for (int i = getRecordCount(); i > slot; i--) {
			copyRecord(recordLength, i - 1, i);
		}

		// write slot metadata
		int pos = getSlotPosition(slot);
		bb.putShort(pos, (short) offset);
		bb.putShort(pos + 2, (short) recordLength);

		// write record
		int initialRecordPos;
		int recordPos = getPhysicalOffset(offset, recordLength);
		initialRecordPos = recordPos;
		bb.position(recordPos);

		recordPos += encodeVarNumber(bb, recordPos, valueLength);
		recordPos += encodeVarNumber(bb, recordPos, keyLength);
		recordPos += encodeBytes(bb, recordPos, key.getBytes());
		recordPos += encodeBytes(bb, recordPos, valueBytes);

		assert initialRecordPos - recordPos != recordLength : "buffer overflow";

		setRecordCount(getRecordCount() + 1);
		return true;
	}

	public boolean delete(int slot) {
		int recordCount = getRecordCount();
		if (slot >= recordCount || slot < 0)
			return false;

		// if it is not last slot, move data
		if (slot != recordCount - 1) {
			int pos = getSlotPosition(slot);
			int length = bb.getShort(pos + 2) & 0xFFFF;

			for (int i = slot; i < recordCount - 1; i++) {
				copyRecord(-length, i + 1, i);
			}
		}

		setRecordCount(recordCount - 1);

		return true;
	}

	private boolean checkFreeSpace(int recordLength) {
		return getFreeSpace() >= (SLOT_SIZE + recordLength);
	}

	public int getFreeSpace() {
		int count = getRecordCount();
		if (count == 0)
			return schema.getPageSize() - PAGE_HEADER_SIZE;

		int pos = getSlotPosition(count - 1);
		int offset = bb.getShort(pos) & 0xFFFF;
		int length = bb.getShort(pos + 2) & 0xFFFF;
		int phyOffset = getPhysicalOffset(offset, length);

		return phyOffset - (PAGE_HEADER_SIZE + SLOT_SIZE * count);
	}

	private int getPhysicalOffset(int offset, int length) {
		return schema.getPageSize() - offset - length;
	}

	private void copyRecord(int move, int from, int to) {
		int fromSlotPos = getSlotPosition(from);
		int offset = bb.getShort(fromSlotPos) & 0xFFFF;
		int length = bb.getShort(fromSlotPos + 2) & 0xFFFF;

		int newOffset = offset + move;

		// move slot metadata
		int toSlotPos = getSlotPosition(to);
		bb.putShort(toSlotPos, (short) newOffset);
		bb.putShort(toSlotPos + 2, (short) length);

		// move slot data
		int phyNewOffset = getPhysicalOffset(newOffset, length);
		int phyOffset = getPhysicalOffset(offset, length);

		int i = 0;
		try {
			for (; i < length; i++)
				data[i + phyNewOffset] = data[i + phyOffset];
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * returns first matching key (used in index node search)
	 */
	public int findSlot(RowKey searchKey) {
		int count = getRecordCount();

		// Since there is no duplicated key in index, you can just return first
		// matching key.
		for (int slot = 0; slot < count; slot++)
			if (getKey(slot).compareTo(searchKey) == 0)
				return slot;

		return -1;
	}

	public int findSlotBefore(RowKey searchKey) {
		int slot = 0;

		int count = getRecordCount();
		while (slot < count && getKey(slot).compareTo(searchKey) < 0)
			slot++;

		return slot - 1;
	}

	public RowKey getKey(int slot) {
		int recordCount = getRecordCount();
		if (slot >= recordCount || slot < 0)
			return null;

		int pos = getSlotPosition(slot);
		int offset = bb.getShort(pos) & 0xFFFF;
		int length = bb.getShort(pos + 2) & 0xFFFF;

		int phyOffset = getPhysicalOffset(offset, length);
		int valueLength = getVarInt(bb, phyOffset);
		int keyLength = getVarInt(bb, phyOffset + NumberEncoder.lengthOf(valueLength));

		byte[] key = new byte[keyLength];
		bb.position(phyOffset + NumberEncoder.lengthOf(keyLength) + NumberEncoder.lengthOf(valueLength));
		bb.get(key);
		return schema.getRowKeyFactory().newKey(key);
	}

	public RowEntry getValue(int slot) {
		if (slot >= getRecordCount() || slot < 0)
			return null;

		int pos = getSlotPosition(slot);
		int offset = bb.getShort(pos) & 0xFFFF;
		int length = bb.getShort(pos + 2) & 0xFFFF;

		int phyOffset = getPhysicalOffset(offset, length);
		int valueLength = getVarInt(bb, phyOffset);
		int keyLength = getVarInt(bb, phyOffset + NumberEncoder.lengthOf(valueLength));

		if (valueLength < 0)
			throw new IllegalStateException("corrupted value length: " + valueLength);

		byte[] b = new byte[valueLength];
		int valuePosition = phyOffset + NumberEncoder.lengthOf(valueLength) + NumberEncoder.lengthOf(keyLength)
				+ keyLength;
		bb.position(valuePosition);
		bb.get(b);
		return schema.getRowValueFactory().newValue(b);
	}

	private static int getVarInt(ByteBuffer bb, int phyOffset) {
		return (int) NumberEncoder.decode(ByteBuffer.wrap(bb.array(), phyOffset, bb.limit() - phyOffset));
	}

	// private static long getVarLong(ByteBuffer bb, int phyOffset) {
	// return NumberEncoder.decode(ByteBuffer.wrap(bb.array(), phyOffset,
	// bb.limit() - phyOffset));
	// }
	//
	// private static short getVarShort(ByteBuffer bb, int phyOffset) {
	// return (short) NumberEncoder.decode(ByteBuffer.wrap(bb.array(),
	// phyOffset, bb.limit() - phyOffset));
	// }

	private int getSlotPosition(int slot) {
		// slot size = payload offset (2) + length (2)
		return PAGE_HEADER_SIZE + slot * SLOT_SIZE;
	}

	//
	// page header
	//

	public boolean leaf() {
		return (bb.getShort(0) & PageType.LEAF) != 0;
	}

	public boolean intKey() {
		return (bb.getShort(0) & PageType.INTKEY) != 0;
	}

	public boolean overflowPage() {
		return (bb.getShort(0) & PageType.OVERFLOW) != 0;
	}

	public short getFlag() {
		return bb.getShort(0);
	}

	public boolean getFlag(short type) {
		return (bb.getShort(0) & type) != 0;
	}

	public void setFlag(short type, boolean flag) {
		if (flag)
			bb.putShort(0, (short) (getFlag() | type));
		else
			bb.putShort(0, (short) (getFlag() & ~type));
	}

	public void clearAllFlag() {
		bb.putShort(0, (short) 0);
	}

	public void setFlag(short flag) {
		bb.putShort(0, flag);
	}

	public int getRecordCount() {
		return bb.getShort(2);
	}

	private void setRecordCount(int count) {
		bb.putShort(2, (short) count);
		dirty = true;
	}

	public int getLeftPage() {
		return bb.getInt(4);
	}

	public void setLeftPage(int pageNumber) {
		bb.putInt(4, pageNumber);
		dirty = true;
	}

	public int getRightPage() {
		return bb.getInt(8);
	}

	public void setRightPage(int pageNumber) {
		bb.putInt(8, pageNumber);
		dirty = true;
	}

	public int getRightChildPage() {
		return bb.getInt(12);
	}

	public void setRightChildPage(int pageNumber) {
		bb.putInt(12, pageNumber);
		dirty = true;
	}

	public int getUpperPage() {
		return bb.getInt(16);
	}

	public void setUpperPage(int pageNumber) {
		bb.putInt(16, pageNumber);
		dirty = true;
	}

	@Override
	public String toString() {
		return "Page " + getNumber();
	}

	private int encodeBytes(ByteBuffer bb, int recordPos, byte[] array) {
		int encodedLen = array.length;
		ByteBuffer.wrap(bb.array(), recordPos, encodedLen).put(array);
		return encodedLen;
	}

	private int encodeVarNumber(ByteBuffer bb, int recordPos, int valueLength) {
		int encodedLen = NumberEncoder.lengthOf(valueLength);
		NumberEncoder.encode(ByteBuffer.wrap(bb.array(), recordPos, encodedLen), valueLength);
		return encodedLen;
	}

	private int getRecordHeaderSize(int keyLength, int valueLength) {
		return NumberEncoder.lengthOf(keyLength) + NumberEncoder.lengthOf(valueLength);
	}

}
