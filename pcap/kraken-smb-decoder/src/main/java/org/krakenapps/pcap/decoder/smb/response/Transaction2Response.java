package org.krakenapps.pcap.decoder.smb.response;
import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x32
public class Transaction2Response implements SmbData{

	boolean malformed = false;
	byte wordCount;
	short byteCount;
	//if final Response
	//WordCount is not 0x00
	//ByteCount is not 0x0000
	// add follow
	//param
	short totalParameterCount;
	short totalDataCount;
	short reserved1;
	short parameterCount;
	short parameterOffset;
	short parameterDisplacement;
	short dataCount;
	short dataOffset;
	short dataDisplacement;
	byte setupCount;
	byte Resreved2;
	byte []setup; // new SetupCount;
	TransData transaction2Data;
	//datas
	byte []pad1;
	byte []trans2Parameter; // new parameterCount;
	byte []pad2;
	byte []trans2Data; //new DataCount;
	
	public byte[] getTrans2Parameter() {
		return trans2Parameter;
	}
	public void setTrans2Parameter(byte[] trans2Parameter) {
		this.trans2Parameter = trans2Parameter;
	}
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
	public short getByteCount() {
		return byteCount;
	}
	public void setByteCount(short byteCount) {
		this.byteCount = byteCount;
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
	public short getReserved1() {
		return reserved1;
	}
	public void setReserved1(short reserved1) {
		this.reserved1 = reserved1;
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
	public byte getSetupCount() {
		return setupCount;
	}
	public void setSetupCount(byte setupCount) {
		this.setupCount = setupCount;
	}
	public byte getResreved2() {
		return Resreved2;
	}
	public void setResreved2(byte resreved2) {
		Resreved2 = resreved2;
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
	@Override
	public String toString(){
		return String.format("First Level : Transaction 2 Response\n" +
				"isMalformed = %s\n" +
				"wordCount = 0x%s(it must 0x00)\n" +
				"byteCount = 0x%s\n" +
				"totalParameterCount = 0x%s , totalDataCount = 0x%s, reserved1 = 0x%s\n" +
				"parameterCount = 0x%s , parameterOffset = 0x%s , parameterDisplacement = 0x%s\n" +
				"dataCount = 0x%s, dataOffset = 0x%s , dataDisplacement = 0x%s\n" +
				"setupCount = 0x%s , reserved2 = 0x%s" +
				"transaction2Data = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.byteCount),
				Integer.toHexString(this.totalParameterCount) , Integer.toHexString(this.totalDataCount) , Integer.toHexString(this.reserved1),
				Integer.toHexString(this.parameterCount) , Integer.toHexString(this.parameterOffset) , Integer.toHexString(this.parameterDisplacement),
				Integer.toHexString(this.dataCount) , Integer.toHexString(this.dataOffset) , Integer.toHexString(this.dataDisplacement),
				Integer.toHexString(this.setupCount) , Integer.toHexString(this.Resreved2),
				this.transaction2Data);
	}
}
