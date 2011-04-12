package org.krakenapps.pcap.decoder.smb.structure;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.pcap.util.Buffer;
import org.krakenapps.pcap.util.ByteOrderConverter;

public class SmbFeaList {
	int sizeOfListInBytes;
	SmbFea fea;
	List<SmbFea> feaList = new ArrayList<SmbFea>();
	public void parse(Buffer b){
		sizeOfListInBytes = ByteOrderConverter.swap(b.getInt());
		while(b.readableBytes()!=0 && sizeOfListInBytes !=0){
			fea=new SmbFea();
			fea.parse(b);
			feaList.add(fea);
		}
	}
	public int getSizeOfListInBytes() {
		return sizeOfListInBytes;
	}
	public void setSizeOfListInBytes(int sizeOfListInBytes) {
		this.sizeOfListInBytes = sizeOfListInBytes;
	}
	public SmbFea getFea() {
		return fea;
	}
	public void setFea(SmbFea fea) {
		this.fea = fea;
	}
	public List<SmbFea> getFeaList() {
		return feaList;
	}
	public void setFeaList(List<SmbFea> feaList) {
		this.feaList = feaList;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
