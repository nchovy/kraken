package org.krakenapps.pcap.decoder.smb.request;
import org.krakenapps.pcap.decoder.smb.structure.SmbData;
//0x27
public class IOCTLRequest implements SmbData{
	boolean malformed = false;
	//param
	private byte wordCount;
	private short fid;
	private short category;
	private short function;
	private short totalParameterCount;
	private short totalDataCount;
	private short maxParameterCount;
	private short maxDataCount;
	private int timeout;
	private short reserved;
	private short parameterCount;
	private short parameterOffset;
	private short dataCount;
	private short dataOffset;
	
	//data
	private short byteCount;
	private byte []pad1;
	private byte []parameters; // new ParameterCount
	private byte []pad2;
	private byte []data; // new DataCount
	public byte getWordCount() {
		return wordCount;
	}
	public void setWordCount(byte wordCount) {
		this.wordCount = wordCount;
	}
	public short getFid() {
		return fid;
	}
	public void setFid(short fid) {
		this.fid = fid;
	}
	public short getCategory() {
		return category;
	}
	public void setCategory(short category) {
		this.category = category;
	}
	public short getFunction() {
		return function;
	}
	public void setFunction(short function) {
		this.function = function;
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
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public short getReserved() {
		return reserved;
	}
	public void setReserved(short reserved) {
		this.reserved = reserved;
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
		return String.format("First Level : IOCTL Request\n"+
				"isMalformed = %s\n"+
				"wordCount = 0x%s\n"+
				"fid = 0x%s, category = 0x%s , function =0x%s\n"+
				"totalParameterCount = 0x%s , totalDataCount = 0x%s , maxParameterCount = 0x%s\n"+
				"maxDataCount = 0x%s , timeOut = 0x%s , reserved = 0x%s\n"+
				"parameterCount = 0x%s , paramterOffset = 0x%s , dataCount = 0x%s , dataOffset = 0x%s"+
				"byteCount = 0x%s\n"+
				"pad1 = %s\n , paramters = %s\n , pad2 = %s\n , data = %s\n",
				this.malformed,
				Integer.toHexString(this.wordCount),
				Integer.toHexString(this.fid) , Integer.toHexString(this.category) , Integer.toHexString(this.function),
				Integer.toHexString(this.totalParameterCount) , Integer.toHexString(this.totalDataCount) , Integer.toHexString(this.maxParameterCount),
				Integer.toHexString(this.maxDataCount) , Integer.toHexString(this.timeout) , Integer.toHexString(this.reserved),
				Integer.toHexString(this.parameterCount) , Integer.toHexString(this.parameterOffset) , Integer.toHexString(this.dataCount) , Integer.toHexString(this.dataOffset),
				Integer.toHexString(this.byteCount),
				this.pad1 , this.parameters , this.pad2 , this.data);
	}
}
