package org.krakenapps.confdb.file;

/**
 * config metadata of manifest file
 * 
 * @author xeraph
 * 
 */
class ConfigEntry {
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
}
