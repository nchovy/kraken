package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x33
public class Transaction2SecondaryRequest implements SmbData{

	boolean malformed = false;
	//param
	byte WordCount;
	short totalParameterCount;
	short totalDataCount;
	short parameterCount;
	short parameterOffset;
	short parameterDisplacement;
	short dataCount;
	short dataOffset;
	short DataDisplacement;
	short fid;
	//data
	short byteCount;
	byte []pad1;
	byte []trans2Parameters; // new parameterCount;
	byte []pad2;
	byte []trans2Data;// new DataCount;
	public byte getWordCount() {
		return WordCount;
	}
	public void setWordCount(byte wordCount) {
		WordCount = wordCount;
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
	public short getParameterOffset() {
		return parameterOffset;
	}
	public void setParameterOffset(short parameterOffset) {
		this.parameterOffset = parameterOffset;
	}
	public short getParameterDisplacement() {
		return parameterDisplacement;
	}
	public void setParameterDisplacement(short parameterDisplacement) {
		this.parameterDisplacement = parameterDisplacement;
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
		return DataDisplacement;
	}
	public void setDataDisplacement(short dataDisplacement) {
		DataDisplacement = dataDisplacement;
	}
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
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
	public byte[] getTrans2Parameters() {
		return trans2Parameters;
	}
	public void setTrans2Parameters(byte[] trans2Parameters) {
		this.trans2Parameters = trans2Parameters;
	}
	public byte[] getPad2() {
		return pad2;
	}
	public void setPad2(byte[] pad2) {
		this.pad2 = pad2;
	}
	public byte[] getTrans2Data() {
		return trans2Data;
	}
	public void setTrans2Data(byte[] trans2Data) {
		this.trans2Data = trans2Data;
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
	// there is no response , no error code;
	@Override
	public String toString(){
		return String.format("First Level : Transaction2 Secondary Request\n" +
				"isMalforemd = %s\n" +
				"wordCount = 0x%s\n" +
				"totalParameterCount = 0x%s , totalDataCount = 0x%s, parameterCount = 0x%s\n" +
				"parameterOffset = 0x%s , parameterDisplacement = 0x%s , dataCount = 0x%s\n" +
				"dataOffset = 0x%s , dataDisplaeMent = 0x%s , fid = 0x%s\n" +
				"byteCount = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.WordCount),
				Integer.toHexString(this.totalParameterCount),
				Integer.toHexString(this.totalDataCount),
				Integer.toHexString(this.parameterCount),
				Integer.toHexString(this.parameterOffset) , Integer.toHexString(this.parameterDisplacement) , Integer.toHexString(this.dataCount),
				Integer.toHexString(this.dataOffset), Integer.toHexString(this.DataDisplacement), Integer.toHexString(this.fid),
				Integer.toHexString(this.byteCount));
	}
}
