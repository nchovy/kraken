package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class NtTransactResponse implements SmbData{

	
	boolean malformed = false;
	byte wordCount; // it must 0x00
	short byteCount; // it must 0x0000
	//if no end multiple message ,add follow
	//param
	byte []reserved1 = new byte[3];
	int totalParameterCount;
	int totalDataCount;
	int parameterCount;
	int parameterOffset;
	int parameterDisplacement;
	int DataCount;
	int DataOffset;
	int DataDisplacement;
	byte setupCount;
	byte []setup; // enw SetupCount;
	TransData ntTransactionData;
	//data
	byte []pad1;
	byte []ntTransParameters; // new ParameterCount;
	byte []pad2;
	byte []ntTransData; //new DataCount;
	
	public TransData getNtTransactionData() {
		return ntTransactionData;
	}
	public void setNtTransactionData(TransData ntTransactionData) {
		this.ntTransactionData = ntTransactionData;
	}
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
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
		return DataCount;
	}
	public void setDataCount(int dataCount) {
		DataCount = dataCount;
	}
	public int getDataOffset() {
		return DataOffset;
	}
	public void setDataOffset(int dataOffset) {
		DataOffset = dataOffset;
	}
	public int getDataDisplacement() {
		return DataDisplacement;
	}
	public void setDataDisplacement(int dataDisplacement) {
		DataDisplacement = dataDisplacement;
	}
	public byte getSetupCount() {
		return setupCount;
	}
	public void setSetupCount(byte setupCount) {
		this.setupCount = setupCount;
	}
	public byte[] getSetup() {
		return setup;
	}
	public void setSetup(byte[] setup) {
		this.setup = setup;
	}
	public byte[] getPad1() {
		return pad1;
	}
	public void setPad1(byte[] pad1) {
		this.pad1 = pad1;
	}
	public byte[] getNtTransParameters() {
		return ntTransParameters;
	}
	public void setNtTransParameters(byte[] ntTransParameters) {
		this.ntTransParameters = ntTransParameters;
	}
	public byte[] getPad2() {
		return pad2;
	}
	public void setPad2(byte[] pad2) {
		this.pad2 = pad2;
	}
	public byte[] getNtTransData() {
		return ntTransData;
	}
	public void setNtTransData(byte[] ntTransData) {
		this.ntTransData = ntTransData;
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
		return String.format("First Level : NtTransactResponse\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s(it must 0x00)\n" +
				"byteCount = 0x%s\n" +
				"reserved = %s , totalParameterCount = 0x%s , totalDataCount = 0x%s\n" +
				"parameterCount = 0x%s , parameterDispalcement = 0x%s , DataCount = 0x%s\n" +
				"DataOffset = 0x%s , DataDisplacement = 0x%s , setupCount = 0x%s\n" +
				"ntTransactionData = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.byteCount),
				this.reserved1.toString(), Integer.toHexString(this.totalParameterCount) , Integer.toHexString(this.totalDataCount),
				Integer.toHexString(this.parameterCount) , Integer.toHexString(this.parameterDisplacement) , Integer.toHexString(this.DataCount),
				Integer.toHexString(this.DataOffset) , Integer.toHexString(this.DataDisplacement) , Integer.toHexString(this.setupCount),
				this.ntTransactionData);
	}
}
