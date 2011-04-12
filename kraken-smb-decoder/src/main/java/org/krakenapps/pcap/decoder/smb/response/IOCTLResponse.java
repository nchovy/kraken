package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x27
public class IOCTLResponse implements SmbData{

	boolean malformed = false;
	byte wordCount;
	short totalParameterCount;
	short totalDataCount;
	short parameterCount;
	short parameterOffset;
	short parameterDisplacement;
	short dataCount;
	short dataOffset;
	short dataDisplacement;
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
		return String.format("First Level : IOCTL Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"totalParameterCount = 0x%s , totalDataCount = 0x%s , parameterCount = 0x%s\n" +
				"parameterOffset = 0x%s , parameterDisplacement = 0x%s, dataCount = 0x%s\n" +
				"dataOffset = 0x%s , dataDisplacement = 0x%s\n" +
				"byteCount = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.totalParameterCount), Integer.toHexString(this.totalDataCount) , Integer.toHexString(this.parameterCount),
				Integer.toHexString(this.parameterOffset) , Integer.toHexString(this.parameterDisplacement), Integer.toHexString(this.dataCount),
				Integer.toHexString(this.dataOffset) , Integer.toHexString(this.dataDisplacement),
				Integer.toHexString(this.byteCount));
	}
}