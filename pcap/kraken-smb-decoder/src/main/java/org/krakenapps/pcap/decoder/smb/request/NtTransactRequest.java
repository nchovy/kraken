package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;

public class NtTransactRequest implements SmbData{
	boolean malformed = false;
	//param
	byte wordCount;
	byte maxCount;
	short reserved1;
	int totalparameterCount;
	int totalDataCount;
	int maxParameterCount;
	int maxDataCount;
	int parameterCount;
	int parameterOffset;
	int dataCount;
	int dataOffset;
	byte setupCount;
	short function;
	byte []setup; // new SetupCount;
	TransData ntTransactionData;
	//data
	short byteCount;
	byte []pad1;
	byte []ntTransParameters; // new ParameterCount
	byte []pad2;
	byte []ntTransData; // new dataCount;
	
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
	public byte getMaxCount() {
		return maxCount;
	}
	public void setMaxCount(byte maxCount) {
		this.maxCount = maxCount;
	}
	public short getReserved1() {
		return reserved1;
	}
	public void setReserved1(short reserved1) {
		this.reserved1 = reserved1;
	}
	public int getTotalparameterCount() {
		return totalparameterCount;
	}
	public void setTotalparameterCount(int totalparameterCount) {
		this.totalparameterCount = totalparameterCount;
	}
	public int getTotalDataCount() {
		return totalDataCount;
	}
	public void setTotalDataCount(int totalDataCount) {
		this.totalDataCount = totalDataCount;
	}
	public int getMaxParameterCount() {
		return maxParameterCount;
	}
	public void setMaxParameterCount(int maxParameterCount) {
		this.maxParameterCount = maxParameterCount;
	}
	public int getMaxDataCount() {
		return maxDataCount;
	}
	public void setMaxDataCount(int maxDataCount) {
		this.maxDataCount = maxDataCount;
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
	public byte getSetupCount() {
		return setupCount;
	}
	public void setSetupCount(byte setupCount) {
		this.setupCount = setupCount;
	}
	public short getFunction() {
		return function;
	}
	public void setFunction(short function) {
		this.function = function;
	}
	public byte[] getSetup() {
		return setup;
	}
	public void setSetup(byte[] setup) {
		this.setup = setup;
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
		return String.format("First Level : Nt Transact Request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"maxCount = 0x%s , reserved1 = 0x%s , totalparameterCount = 0x%s\n"+
				"totalDataCount = 0x%s , maxParameterCount = 0x%s , maxDataCount = 0x%s\n"+
				"parameterCount = 0x%s,  parameterOffset = 0x%s , dataCount = 0x%s\n"+
				"dataOffset = 0x%s , setupCount = %s \n"+
				"byteCount = 0x%s\n"+
				"TransData can't showed because it can be null\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.maxCount),Integer.toHexString(this.reserved1) , Integer.toHexString(this.totalparameterCount),
				Integer.toHexString(this.totalDataCount) , Integer.toHexString(this.maxParameterCount) , Integer.toHexString(this.maxDataCount),
				Integer.toHexString(this.parameterCount) , Integer.toHexString(this.parameterOffset) , Integer.toHexString(this.dataCount),
				Integer.toHexString(this.dataOffset) , this.setup.toString(),
				Integer.toHexString(this.byteCount));
	}
}
