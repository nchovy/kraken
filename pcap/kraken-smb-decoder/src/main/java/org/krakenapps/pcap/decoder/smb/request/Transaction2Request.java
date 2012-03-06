package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x32
public class Transaction2Request implements SmbData{
	boolean malformed = false;
	//param
	byte wordCount;
	int totalParameterCount;
	int totalDataCount;
	short maxParameterCount;
	int maxDataCount;
	byte maxSetupCount;
	byte reserved1;
	short flags;
	int timeout;
	int Reserved2;
	int parameterCount;
	int parameterOffset;
	int dataCount;
	int dataOffset;
	byte setupCount;
	byte reserved3;
	TransData transaction2Data;
	byte []setup; // new SetupCount;
	//data
	short byteCount;
	String name;
	byte []pad1;
	byte []trans2Parameters; // new ParameterCount;
	byte []pad2;
	byte []trans2Data; // new DataCount;
	
	public TransData getTransaction2Data() {
		return transaction2Data;
	}
	public void setTransaction2Data(TransData transaction2Data) {
		this.transaction2Data = transaction2Data;
	}
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public int getTotalParameterCount() {
		return totalParameterCount;
	}
	public void setTotalParameterCount(int totalParameterCount) {
		if(totalParameterCount <0){
			totalParameterCount = ~totalParameterCount & 0x0000ffff;
		}
		this.totalParameterCount = totalParameterCount;
	}
	public int getTotalDataCount() {
		return totalDataCount;
	}
	public void setTotalDataCount(int totalDataCount) {
		if(totalDataCount < 0 ){
			totalDataCount = ~totalDataCount & 0x0000ffff;
		}
		this.totalDataCount = totalDataCount;
	}
	public short getMaxParameterCount() {
		return maxParameterCount;
	}
	public void setMaxParameterCount(short maxParameterCount) {
		this.maxParameterCount = maxParameterCount;
	}
	public int getMaxDataCount() {
		return maxDataCount;
	}
	public void setMaxDataCount(short maxDataCount) {
		this.maxDataCount = maxDataCount;
	}
	public byte getMaxSetupCount() {
		return maxSetupCount;
	}
	public void setMaxSetupCount(byte maxSetupCount) {
		this.maxSetupCount = maxSetupCount;
	}
	public byte getReserved1() {
		return reserved1;
	}
	public void setReserved1(byte reserved1) {
		this.reserved1 = reserved1;
	}
	public short getFlags() {
		return flags;
	}
	public void setFlags(short flags) {
		this.flags = flags;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public int getReserved2() {
		return Reserved2;
	}
	public void setReserved2(short reserved2) {
		Reserved2 = reserved2;
	}
	public int getParameterCount() {
		return parameterCount;
	}
	public void setParameterCount(short parameterCount) {
		this.parameterCount = parameterCount;
	}
	public int getParameterOffset() {
		return parameterOffset;
	}
	public void setParameterOffset(int parameterOffset) {
		if(parameterOffset <0){
			parameterOffset = ~parameterOffset & 0x0000ffff;
		}
		this.parameterOffset = parameterOffset;
	}
	public int getDataCount() {
		return dataCount;
	}
	public void setDataCount(short dataCount) {
		this.dataCount = dataCount;
	}
	public int getDataOffset() {
		return dataOffset;
	}
	public void setDataOffset(short dataOffset) {
		this.dataOffset = dataOffset;
	}
	public byte getSetupCount() {
		return setupCount;
	}
	public void setSetupCount(byte setupCount) {
		this.setupCount = setupCount;
	}
	public byte getReserved3() {
		return reserved3;
	}
	public void setReserved3(byte reserved3) {
		this.reserved3 = reserved3;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
		return malformed;
	}
	@Override
	public void setMalformed(boolean malformed) {
		this.malformed = malformed;
	}
	@Override
	public String toString(){
		return String.format("First Level : Transaction 2 Request\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s\n" +
				"totalParameterCount = 0x%s , totalDataCount = 0x%s,  maxParameterCount = 0x%s\n" +
				"maxDataCount = 0x%s,  maxSetupcount = 0x%s, reserved1 = 0x%s\n" +
				"flags = 0x%s , timeOut = 0x%s , reserved2 = 0x%s\n" +
				"parameterCount = 0x%s , parameteroffset = 0x%s,  dataCount = 0x%s\n" +
				"dataOffset = 0x%s , setupCount = 0x%s , reserved3 = 0x%s\n" +
				"byteCount = 0x%s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.totalParameterCount) , Integer.toHexString(this.totalDataCount) , Integer.toHexString(this.maxParameterCount),
				Integer.toHexString(this.maxDataCount) , Integer.toHexString(this.maxSetupCount) , Integer.toHexString(this.reserved1),
				Integer.toHexString(this.flags) , Integer.toHexString(this.timeout) , Integer.toHexString(this.Reserved2),
				Integer.toHexString(this.parameterCount) , Integer.toHexString(this.parameterOffset) , Integer.toHexString(this.dataCount),
				Integer.toHexString(this.dataOffset) , Integer.toHexString(this.setupCount) , Integer.toHexString(this.reserved3),
				Integer.toHexString(this.byteCount));
	}
}
