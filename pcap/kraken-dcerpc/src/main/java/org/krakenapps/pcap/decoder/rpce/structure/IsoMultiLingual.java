package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;

public class IsoMultiLingual {

	private byte row;
	private byte column;
	public byte getRow() {
		return row;
	}
	public void setRow(byte row) {
		this.row = row;
	}
	public byte getColumn() {
		return column;
	}
	public void setColumn(byte column) {
		this.column = column;
	}
	public void parse(Buffer b){
		row = b.get();
		column = b.get();
	}
}
