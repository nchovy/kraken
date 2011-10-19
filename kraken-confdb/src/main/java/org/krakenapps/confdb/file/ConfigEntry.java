package org.krakenapps.confdb.file;

/**
 * element of manifest file
 * 
 * @author xeraph
 * 
 */
class ConfigEntry {
	private int colId;
	private int docId;
	private int rev;

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

	public int getRev() {
		return rev;
	}

	public void setRev(int rev) {
		this.rev = rev;
	}

}
