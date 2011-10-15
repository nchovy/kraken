package org.krakenapps.confdb;

public enum CommitOp {
	CreateDoc(1), UpdateDoc(2), DeleteDoc(3), CreateCol(4), DropCol(5);

	private int code;

	CommitOp(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
	public static CommitOp parse(int code) {
		for (CommitOp op : values())
			if (op.getCode() == code)
				return op;
		
		return null;
	}
}
