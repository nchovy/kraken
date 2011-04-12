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
package org.krakenapps.btree;

import java.nio.ByteBuffer;

public class Page {
	// (flag, count), left, right, right-child, upper
	public static final int PAGE_HEADER_SIZE = 20;
	public static final int SLOT_SIZE = 8;

	// record = key length(4) + key + value
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

		int reserve = RECORD_HEADER_SIZE + keyLength + valueLength;
		if (!checkFreeSpace(reserve))
			return false;

		int before = findSlotBefore(key);
		int slot = before + 1;

		// calculate new offset
		int offset = 0;
		if (before >= 0) {
			int pos = getSlotPosition(before);
			int beforeOffset = bb.getInt(pos);
			int beforeLength = bb.getInt(pos + 4);
			offset = beforeOffset + beforeLength;
		}

		// move existing data
		for (int i = getRecordCount(); i > slot; i--) {
			copyRecord(reserve, i - 1, i);
		}

		// write slot metadata
		int pos = getSlotPosition(slot);
		bb.putInt(pos, offset);
		bb.putInt(pos + 4, reserve);

		// write row data
		bb.position(getPhysicalOffset(offset, reserve));
		bb.putInt(keyLength);
		bb.put(key.getBytes());
		bb.put(valueBytes);

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
			int length = bb.getInt(pos + 4);

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
		int offset = bb.getInt(pos);
		int length = bb.getInt(pos + 4);
		int phyOffset = getPhysicalOffset(offset, length);

		return phyOffset - (PAGE_HEADER_SIZE + SLOT_SIZE * count);
	}

	private int getPhysicalOffset(int offset, int length) {
		return schema.getPageSize() - offset - length;
	}

	private void copyRecord(int move, int from, int to) {
		int pos = getSlotPosition(from);
		int offset = bb.getInt(pos);
		int length = bb.getInt(pos + 4);

		// move slot metadata
		int newPos = getSlotPosition(to);
		int newOffset = offset + move;
		bb.putInt(newPos, newOffset);
		bb.putInt(newPos + 4, length);

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
		int offset = bb.getInt(pos);
		int length = bb.getInt(pos + 4);

		int phyOffset = getPhysicalOffset(offset, length);
		int keyLength = bb.getInt(phyOffset);

		byte[] key = new byte[keyLength];
		bb.position(phyOffset + 4);
		bb.get(key);
		return schema.getRowKeyFactory().newKey(key);
	}

	public RowEntry getValue(int slot) {
		if (slot >= getRecordCount() || slot < 0)
			return null;

		int pos = getSlotPosition(slot);
		int offset = bb.getInt(pos);
		int length = bb.getInt(pos + 4);

		int phyOffset = getPhysicalOffset(offset, length);
		int keyLength = bb.getInt(phyOffset);
		int valueLength = length - RECORD_HEADER_SIZE - keyLength;

		if (valueLength < 0)
			throw new IllegalStateException("corrupted value length: " + valueLength);

		byte[] b = new byte[valueLength];
		int valuePosition = phyOffset + RECORD_HEADER_SIZE + keyLength;
		bb.position(valuePosition);
		bb.get(b);
		return schema.getRowValueFactory().newValue(b);
	}

	private int getSlotPosition(int slot) {
		// slot size = payload offset (4) + length (4)
		return PAGE_HEADER_SIZE + slot * SLOT_SIZE;
	}

	//
	// page header
	//

	public int getFlag() {
		return bb.getShort(0);
	}

	public void setFlag(int flag) {
		bb.putShort(0, (short) flag);
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
}
