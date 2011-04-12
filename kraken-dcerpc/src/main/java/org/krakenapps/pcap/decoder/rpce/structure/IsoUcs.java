package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;

public class IsoUcs {

	private byte group;
	private byte plane;
	private byte row;
	private byte column;

	public void parse(Buffer b) {
		group = b.get();
		plane = b.get();
		row = b.get();
		column = b.get();
	}

	public byte getGroup() {
		return group;
	}

	public void setGroup(byte group) {
		this.group = group;
	}

	public byte getPlane() {
		return plane;
	}

	public void setPlane(byte plane) {
		this.plane = plane;
	}

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
}
