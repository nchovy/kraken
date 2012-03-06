package org.krakenapps.pcap.decoder.smb.request;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.util.Buffer;

//0x25
public class TransactionRequest implements SmbData {

	boolean malformed = false;
	// param
	byte wordCount;
	short totalParameterCount;
	short totalDataCount;
	short maxParameterCount;
	short maxDataCount;
	byte maxSetupCount;
	byte reserved1;
	short flags;
	int timeout;
	short reserved2;
	short parameterCount;
	short parameterOffset;
	short dataCount;
	short dataOffset;
	byte setupCount;
	byte reserved3;
	TransData transactionData;
	byte[] setup;// subcommand
	// data
	short byteCount;
	String name;
	byte[] pad1;
	byte[] transParameters;
	byte[] pad2;;
	byte[] transData;
	Buffer upper;

	public TransData getTransactionData() {
		return transactionData;
	}

	public void setTransactionData(TransData transactionData) {
		this.transactionData = transactionData;
	}

	/* if error exist */
	public byte getWordCount() {
		return wordCount;
	}

	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}

	public short getTotalParameterCount() {
		return totalParameterCount;
	}

	public void setTotalParameterCount(short totlaParameterCount) {
		this.totalParameterCount = totlaParameterCount;
	}

	public short getTotalDataCount() {
		return totalDataCount;
	}

	public void setTotalDataCount(short totalDataCount) {
		this.totalDataCount = totalDataCount;
	}

	public short getMaxParameterCount() {
		return maxParameterCount;
	}

	public void setMaxParameterCount(short maxParameterCount) {
		this.maxParameterCount = maxParameterCount;
	}

	public short getMaxDataCount() {
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

	public short getReserved2() {
		return reserved2;
	}

	public void setReserved2(short reserved2) {
		this.reserved2 = reserved2;
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

	public byte[] getTransParameters() {
		return transParameters;
	}

	public void setTransParameters(byte[] transParameters) {
		this.transParameters = transParameters;
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

	public Buffer getUpper() {
		return upper;
	}

	public void setUpper(Buffer upper) {
		this.upper = upper;
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
	public String toString() {
		return String
				.format("First Level : Transaction Request\n"
						+ "isMalformed = %s\n"
						+ "wordCount = 0x%s\n"
						+ "totalParameterCount = 0x%s , totalDataCount = 0x%s,  maxParameterCount = 0x%s\n"
						+ "maxDataCount = 0x%s,  maxSetupcount = 0x%s, reserved1 = 0x%s\n"
						+ "flags = 0x%s , timeOut = 0x%s , reserved2 = 0x%s\n"
						+ "parameterCount = 0x%s , parameteroffset = 0x%s,  dataCount = 0x%s\n"
						+ "dataoffset = 0x%s , setupCount = 0x%s , reserved3 = 0x%s\n"
						+ "byteCount = 0x%s\n" + "Name = %s\n", this.malformed,
						Integer.toHexString(this.wordCount),
						Integer.toHexString(this.totalParameterCount),
						Integer.toHexString(this.totalDataCount),
						Integer.toHexString(this.maxParameterCount),
						Integer.toHexString(this.maxDataCount),
						Integer.toHexString(this.maxSetupCount),
						Integer.toHexString(this.reserved1),
						Integer.toHexString(this.flags),
						Integer.toHexString(this.timeout),
						Integer.toHexString(this.reserved2),
						Integer.toHexString(this.parameterCount),
						Integer.toHexString(this.parameterOffset),
						Integer.toHexString(this.dataCount),
						Integer.toHexString(this.dataOffset),
						Integer.toHexString(this.setupCount),
						Integer.toHexString(this.reserved3),
						Integer.toHexString(this.byteCount), this.name);
	}
}
