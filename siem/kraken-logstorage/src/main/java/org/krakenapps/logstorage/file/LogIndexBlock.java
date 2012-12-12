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
package org.krakenapps.logstorage.file;

public class LogIndexBlock {
	// pseudo field, file offset 
	private long filePointer;

	// psuedo field, index number (just for numbering and ordering)
	private int index;

	// physically mapped
	private int count;

	// physically mapped
	private int[] offsets;

	public long getFilePointer() {
		return filePointer;
	}

	public void setFilePointer(long filePointer) {
		this.filePointer = filePointer;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int[] getOffsets() {
		return offsets;
	}

	public void setOffsets(int[] offsets) {
		this.offsets = offsets;
	}

	@Override
	public String toString() {
		return "log index block " + index + ", count=" + count;
	}

}
