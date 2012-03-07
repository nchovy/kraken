package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class NtTransactSecondaryRequest implements SmbData{
	boolean malformed = false;
	byte wordCount;
	byte []reserved1 = new byte[3];
	int totalParameterCount;
	int totalDataCount;
	int parameterCount;
	int parameterOffset;
	int parameterDisplacement;
	int dataCount;
	int dataOffset;
	int dataDisplacement;
	byte reserved2;
	//data
	short byteCount;
	byte []pad1;
	byte []parameters; // new ParameterCount;
	byte []pad2;
	byte []data; // new DataCount;
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public byte[] getReserved1() {
		return reserved1;
	}
	public void setReserved1(byte[] reserved1) {
		this.reserved1 = reserved1;
	}
	public int getTotalParameterCount() {
		return totalParameterCount;
	}
	public void setTotalParameterCount(int totalParameterCount) {
		this.totalParameterCount = totalParameterCount;
	}
	public int getTotalDataCount() {
		return totalDataCount;
	}
	public void setTotalDataCount(int totalDataCount) {
		this.totalDataCount = totalDataCount;
	}
	public int getParameterCount() {
		return parameterCount;
	}
	public void setParameterCount(int parameterCount) {
		this.parameterCount = parameterCount;
	}
	public int getParameterOffset() {
		return parameterOffset;
	}
	public void setParameterOffset(int parameterOffset) {
		this.parameterOffset = parameterOffset;
	}
	public int getParameterDisplacement() {
		return parameterDisplacement;
	}
	public void setParameterDisplacement(int parameterDisplacement) {
		this.parameterDisplacement = parameterDisplacement;
	}
	public int getDataCount() {
		return dataCount;
	}
	public void setDataCount(int dataCount) {
		this.dataCount = dataCount;
	}
	public int getDataOffset() {
		return dataOffset;
	}
	public void setDataOffset(int dataOffset) {
		this.dataOffset = dataOffset;
	}
	public int getDataDisplacement() {
		return dataDisplacement;
	}
	public void setDataDisplacement(int dataDisplacement) {
		this.dataDisplacement = dataDisplacement;
	}
	public byte getReserved2() {
		return reserved2;
	}
	public void setReserved2(byte reserved2) {
		this.reserved2 = reserved2;
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
	public byte[] getParameters() {
		return parameters;
	}
	public void setParameters(byte[] parameters) {
		this.parameters = parameters;
	}
	public byte[] getPad2() {
		return pad2;
	}
	public void setPad2(byte[] pad2) {
		this.pad2 = pad2;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
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
		return String.format("First Level : Nt Transact Secondary\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"reserved1 = %s , totalParameterCount = 0x%s , totalDataCount = 0x%s\n"+
				"parameterCount = 0x%s , parameteroffset = 0x%s , parameterDisplacement =0x%s\n"+
				"dataCount = 0x%s , dataOffset = 0x%s , dataDisplacement = 0x%s , reserved2 = 0x%s\n"+
				"byteCount = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				this.reserved1.toString(), Integer.toHexString(this.totalParameterCount) , Integer.toHexString(this.totalDataCount),
				Integer.toHexString(this.parameterCount) , Integer.toHexString(this.parameterOffset) , Integer.toHexString(this.parameterDisplacement),
				Integer.toHexString(this.dataCount) , Integer.toHexString(this.dataOffset) , Integer.toHexString(this.dataDisplacement) , Integer.toHexString(this.reserved2),
				Integer.toHexString(this.byteCount));
	}
}
