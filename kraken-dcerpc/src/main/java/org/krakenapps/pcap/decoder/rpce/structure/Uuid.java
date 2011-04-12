package org.krakenapps.pcap.decoder.rpce.structure;

import java.util.Arrays;

import org.krakenapps.pcap.util.Buffer;

public class Uuid {

	byte []buff;
	public Uuid(){
		buff = new byte[16];
	}
	public void parse(Buffer b){
		b.gets(buff);
		return;
	}
	@Override
	public String toString() {
		return "Uuid [buff=" + Arrays.toString(buff) + "]";
	}
	public byte[] getBuff() {
		return buff;
	}
	public void setBuff(byte[] buff) {
		this.buff = buff;
	}
	
}
