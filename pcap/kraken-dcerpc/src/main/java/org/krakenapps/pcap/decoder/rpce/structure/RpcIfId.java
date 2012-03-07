package org.krakenapps.pcap.decoder.rpce.structure;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class RpcIfId {

	private int uid;
	private int versMajor;
	private int versMinor;
	public void parse(Buffer b){
		uid = ByteOrderConverter.swap(b.getInt());
		versMajor = ByteOrderConverter.swap(b.getInt());
		versMinor = ByteOrderConverter.swap(b.getInt());
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public int getVersMajor() {
		return versMajor;
	}
	public void setVersMajor(int versMajor) {
		this.versMajor = versMajor;
	}
	public int getVersMinor() {
		return versMinor;
	}
	public void setVersMinor(int versMinor) {
		this.versMinor = versMinor;
	}
	
}
