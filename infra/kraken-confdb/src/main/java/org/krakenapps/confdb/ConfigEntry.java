/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.confdb;

/**
 * config metadata of manifest file
 * 
 * @author xeraph
 * 
 */
public class ConfigEntry implements Comparable<ConfigEntry> {
	private int colId;
	private int docId;
	private long rev;

	// index of revision log file for direct access
	private int index;

	public ConfigEntry() {
	}

	// legacy format support
	public ConfigEntry(int colId, int docId, long rev) {
		this(colId, docId, rev, 0);
	}

	public ConfigEntry(int colId, int docId, long rev, int index) {
		this.colId = colId;
		this.docId = docId;
		this.rev = rev;
		this.index = index;
	}

	public int getColId() {
		return colId;
	}

	public void setColId(int colId) {
		this.colId = colId;
	}

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public long getRev() {
		return rev;
	}

	public void setRev(long rev) {
		this.rev = rev;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + colId;
		result = prime * result + docId;
		result = prime * result + (int) (rev ^ (rev >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigEntry other = (ConfigEntry) obj;
		if (colId != other.colId)
			return false;
		if (docId != other.docId)
			return false;
		if (rev != other.rev)
			return false;
		return true;
	}

	@Override
	public int compareTo(ConfigEntry e) {
		if (colId != e.getColId())
			return colId - e.getColId();

		if (docId != e.getDocId())
			return docId - e.getDocId();

		if (rev != e.getRev())
			return (int) (rev - e.getRev());

		return 0;
	}

	@Override
	public String toString() {
		return "col=" + colId + ", doc=" + docId + ", rev=" + rev + ", index=" + index;
	}
}
