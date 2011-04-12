package org.krakenapps.pcap.decoder.smb.structure;

public class FileNotifyInformation {

	int nextEntryoffset;
	int action;
	int fileNameLength;
	String fileName;
	public int getNextEntryoffset() {
		return nextEntryoffset;
	}
	public void setNextEntryoffset(int nextEntryoffset) {
		this.nextEntryoffset = nextEntryoffset;
	}
	public int getAction() {
		return action;
	}
	public void setAction(int action) {
		this.action = action;
	}
	public int getFileNameLength() {
		return fileNameLength;
	}
	public void setFileNameLength(int fileNameLength) {
		this.fileNameLength = fileNameLength;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	@Override
	public String toString(){
		return String.format("Structure : FileNotifyInformation\n"+
				"nextOffset = 0x%s , action = 0x%s , fileNamelength = 0x%s\n"+
				"fileName = %s\n",
				Integer.toHexString(this.nextEntryoffset) , Integer.toHexString(this.action) , Integer.toHexString(this.fileNameLength),
				this.fileName);
	}
}
