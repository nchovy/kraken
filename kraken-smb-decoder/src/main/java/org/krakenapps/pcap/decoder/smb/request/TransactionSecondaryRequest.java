package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x26
public class TransactionSecondaryRequest implements SmbData{

	boolean malformed = false;
	//param
	byte wordCount;
	short totalParameterCount;
	short totalDataCount;
	short parameterCount;
	short parameterDisplacement;
	short parameterOffset;
	short dataCount;
	short dataOffset;
	short dataDisplacement;
	//data
	short byteCount;
	byte []pad1;
	byte []transParameters;// new parametercount
	byte []pad2;
	byte []transData; // new DataCount;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getTotalParameterCount() {
		return totalParameterCount;
	}
	public void setTotalParameterCount(short totalParameterCount) {
		this.totalParameterCount = totalParameterCount;
	}
	public short getTotalDataCount() {
		return totalDataCount;
	}
	public void setTotalDataCount(short totalDataCount) {
		this.totalDataCount = totalDataCount;
	}
	public short getParameterCount() {
		return parameterCount;
	}
	public void setParameterCount(short parameterCount) {
		this.parameterCount = parameterCount;
	}
	public short getParameterDisplacement() {
		return parameterDisplacement;
	}
	public void setParameterDisplacement(short parameterDisplacement) {
		this.parameterDisplacement = parameterDisplacement;
	}
	public short getParameterOffset() {
		return parameterOffset;
	}
	public void setParameterOffset(short parameterOffset) {
		this.parameterOffset = parameterOffset;
	}
	public short getDataCount() {
		return dataCount;
	}
	public void setDataCount(short dataCount) {
		this.dataCount = dataCount;
	}
	public short getDataOffset() {
		return dataOffset;
	}
	public void setDataOffset(short dataOffset) {
		this.dataOffset = dataOffset;
	}
	public short getDataDisplacement() {
		return dataDisplacement;
	}
	public void setDataDisplacement(short dataDisplacement) {
		this.dataDisplacement = dataDisplacement;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
	}
	public byte[] getPad1() {
		return pad1;
	}
	public void setPad1(byte[] pad1) {
		this.pad1 = pad1;
	}
	public byte[] getTransParametrs() {
		return transParameters;
	}
	public void setTransParameters(byte[] transParametrs) {
		this.transParameters = transParametrs;
	}
	public byte[] getPad2() {
		return pad2;
	}
	public void setPad2(byte[] pad2) {
		this.pad2 = pad2;
	}
	public byte[] getTransData() {
		return transData;
	}
	public void setTransData(byte[] transData) {
		this.transData = transData;
	}
	@Override
	public boolean isMalformed() {
		// TODO Auto-generated method stub
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}
	@Override
	public String toString(){
		return String.format("First Level : Transaction Secondary Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"totalparameterCount = 0x%s , totalDataCount = 0x%s , parameterCount = 0x%s\n" +
				"parameterDisplacement = 0x%s , parameterOffset = 0x%s , dataCount = 0x%s\n" +
				"dataOffset = 0x%s , dataDisplacement = 0x%s\n" +
				"byteCount = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.totalParameterCount), Integer.toHexString(this.totalDataCount) , Integer.toHexString(this.parameterCount),
				Integer.toHexString(this.parameterDisplacement) , Integer.toHexString(this.parameterOffset) , Integer.toHexString(this.dataCount),
				Integer.toHexString(this.dataOffset) , Integer.toHexString(this.dataDisplacement),
				Integer.toHexString(this.byteCount));
	}
}
