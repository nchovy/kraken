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
public class ConfigEntry {
	private int colId;
	private int docId;
	private long rev;

	public ConfigEntry() {
	}

	public ConfigEntry(int colId, int docId, long rev) {
		this.colId = colId;
		this.docId = docId;
		this.rev = rev;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + colId;
		result = prime * result + docId;
		return result;
	}

	/**
	 * the key is composition of collection id and doc id
	 */
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
		return true;
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

	@Override
	public String toString() {
		return "col=" + colId + ", doc=" + docId + ", rev=" + rev;
	}

}
