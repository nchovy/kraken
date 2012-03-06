package org.krakenapps.pcap.decoder.smb.response;

import org.krakenapps.pcap.decoder.smb.TransData;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
import org.krakenapps.pcap.util.Buffer;

//0x25
public class TransactionResponse implements SmbData {

	boolean malformed = false;
	/* if error exist */
	// param
	byte wordCount;
	// data
	short byteCount;
	/* if error exist */
	/* if no error add follow */
	// param
	short totalParameterCount;
	short totalDataCount;
	short Reserved1;
	short parameterCount;
	short parameterOffset;
	short parameterDisplacement;
	short dataCount;
	short dataOffset;
	short dataDisplacement;
	byte SetupCount;
	byte reserved2;
	byte[] setup; // new SetupCount;
	TransData transactionData;
	// data
	byte[] pad1;
	byte[] transParameters;
	byte[] pad2;
	byte[] transData;
	Buffer upper;

	public TransData getTransactionData() {
		return transactionData;
	}

	public void setTransactionData(TransData transactionData) {
		this.transactionData = transactionData;
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

	public short getParameterDisplacement() {
		return parameterDisplacement;
	}

	public void setParameterDisplacement(short parameterDisplacement) {
		this.parameterDisplacement = parameterDisplacement;
	}

	public Buffer getUpper() {
		return upper;
	}

	public void setUpper(Buffer upper) {
		this.upper = upper;
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
		return Reserved1;
	}

	public void setReserved1(short reserved1) {
		Reserved1 = reserved1;
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

	public short getParamterDisplacement() {
		return parameterDisplacement;
	}

	public void setParamterDisplacement(short paramterDisplacement) {
		this.parameterDisplacement = paramterDisplacement;
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
		return SetupCount;
	}

	public void setSetupCount(byte setupCount) {
		SetupCount = setupCount;
	}

	public byte getReserved2() {
		return reserved2;
	}

	public void setReserved2(byte reserved2) {
		this.reserved2 = reserved2;
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
				.format("First Level : Transaction Response\n"
						+ "isMalformed = %s\n"
						+ "wordCount = 0x%s(it must 0x00)\n"
						+ "byteCount = 0x%%s\n"
						+ "totalParameterCount = 0x%s , totalDataCount = 0x%s,  reserved1 = 0x%s\n"
						+ "parameterCount = 0x%s, parameterOffset = 0x%s, parameterDisplacement = 0x%s\n"
						+ "dataCount = 0x%s, dataOffset = 0x%s, dataDisplacement = 0x%s\n"
						+ "setupCount = 0x%s, reserved2 = 0x%s\n"
						+ "transactionData = %s\n", this.malformed,
						Integer.toHexString(this.wordCount),
						Integer.toHexString(this.byteCount),
						Integer.toHexString(this.totalParameterCount),
						Integer.toHexString(this.totalDataCount),
						Integer.toHexString(this.Reserved1),
						Integer.toHexString(this.parameterCount),
						Integer.toHexString(this.parameterOffset),
						Integer.toHexString(this.parameterDisplacement),
						Integer.toHexString(this.dataCount),
						Integer.toHexString(this.dataOffset),
						Integer.toHexString(this.dataDisplacement),
						Integer.toHexString(this.SetupCount),
						Integer.toHexString(this.reserved2),
						this.transactionData);
	}
}
