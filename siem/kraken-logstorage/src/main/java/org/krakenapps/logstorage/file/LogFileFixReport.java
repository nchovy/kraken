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

import java.io.File;

public class LogFileFixReport {
	private File indexPath;
	private File dataPath;
	private long totalLogCount;
	private int totalIndexBlocks;
	private int totalDataBlocks;
	private int truncatedIndexBlocks;
	private int truncatedIndexBytes;
	private int truncatedDataBytes;
	private int lostLogCount;
	private int addedIndexBlocks;

	public File getIndexPath() {
		return indexPath;
	}

	public void setIndexPath(File indexPath) {
		this.indexPath = indexPath;
	}

	public File getDataPath() {
		return dataPath;
	}

	public void setDataPath(File dataPath) {
		this.dataPath = dataPath;
	}

	public long getTotalLogCount() {
		return totalLogCount;
	}

	public void setTotalLogCount(long totalLogCount) {
		this.totalLogCount = totalLogCount;
	}

	public int getTotalIndexBlocks() {
		return totalIndexBlocks;
	}

	public void setTotalIndexBlocks(int totalIndexBlocks) {
		this.totalIndexBlocks = totalIndexBlocks;
	}

	public int getTotalDataBlocks() {
		return totalDataBlocks;
	}

	public void setTotalDataBlocks(int totalDataBlocks) {
		this.totalDataBlocks = totalDataBlocks;
	}

	public int getTruncatedIndexBlocks() {
		return truncatedIndexBlocks;
	}

	public void setTruncatedIndexBlocks(int truncatedIndexBlocks) {
		this.truncatedIndexBlocks = truncatedIndexBlocks;
	}

	public int getTruncatedIndexBytes() {
		return truncatedIndexBytes;
	}

	public void setTruncatedIndexBytes(int truncatedIndexBytes) {
		this.truncatedIndexBytes = truncatedIndexBytes;
	}

	public int getTruncatedDataBytes() {
		return truncatedDataBytes;
	}

	public void setTruncatedDataBytes(int truncatedDataBytes) {
		this.truncatedDataBytes = truncatedDataBytes;
	}

	public long getLostLogCount() {
		return lostLogCount;
	}

	public void setLostLogCount(int lostLogCount) {
		this.lostLogCount = lostLogCount;
	}

	public int getAddedIndexBlocks() {
		return addedIndexBlocks;
	}

	public void setAddedIndexBlocks(int addedIndexBlocks) {
		this.addedIndexBlocks = addedIndexBlocks;
	}

	@Override
	public String toString() {
		return "index path: " + indexPath.getAbsolutePath() + "\ndata path: " + dataPath.getAbsolutePath()
				+ "\ntotal log count: " + totalLogCount + "\ntotal index blocks: " + totalIndexBlocks + "\ntotal data blocks: "
				+ totalDataBlocks + "\nlost logs: " + lostLogCount + "\ntruncated index blocks: " + truncatedIndexBlocks
				+ "\ntruncated index bytes: " + truncatedIndexBytes + "\ntruncated data bytes: " + truncatedDataBytes
				+ "\nadded index blocks: " + addedIndexBlocks;
	}
}
