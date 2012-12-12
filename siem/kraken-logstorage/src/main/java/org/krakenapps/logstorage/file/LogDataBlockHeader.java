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

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogDataBlockHeader {
	// psuedo index number (just for numbering and ordering)
	private int index;
	private Date minDate;
	private Date maxDate;
	private int originalLength;
	private int compressedLength;
	private long filePointer;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Date getMinDate() {
		return minDate;
	}

	public void setMinDate(Date minDate) {
		this.minDate = minDate;
	}

	public Date getMaxDate() {
		return maxDate;
	}

	public void setMaxDate(Date maxDate) {
		this.maxDate = maxDate;
	}

	public int getOriginalLength() {
		return originalLength;
	}

	public void setOriginalLength(int originalLength) {
		this.originalLength = originalLength;
	}

	public int getCompressedLength() {
		return compressedLength;
	}

	public void setCompressedLength(int compressedLength) {
		this.compressedLength = compressedLength;
	}

	public long getFilePointer() {
		return filePointer;
	}

	public void setFilePointer(long filePointer) {
		this.filePointer = filePointer;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return "data block " + index + " [min date=" + dateFormat.format(minDate) + ", max date="
				+ dateFormat.format(maxDate) + ", original=" + originalLength + ", compressed=" + compressedLength
				+ ", fp=" + filePointer + "]";
	}
}
