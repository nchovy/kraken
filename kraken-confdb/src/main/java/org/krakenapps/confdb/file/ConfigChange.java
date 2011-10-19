package org.krakenapps.confdb.file;

import org.krakenapps.confdb.CommitOp;

class ConfigChange {
	private CommitOp operation;

	private String colName;

	private int colId;

	private int docId;

	public ConfigChange() {
	}

	public ConfigChange(CommitOp operation, String colName, int colId, int docId) {
		this.operation = operation;
		this.colName = colName;
		this.colId = colId;
		this.docId = docId;
	}

	public CommitOp getOperation() {
		return operation;
	}

	public void setOperation(CommitOp operation) {
		this.operation = operation;
	}

	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
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

	@Override
	public String toString() {
		return "op=" + operation + ", col=" + colName + ", doc=" + docId;
	}
}
