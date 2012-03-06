package org.krakenapps.pcap.decoder.smb.transresp;

import org.krakenapps.pcap.decoder.smb.TransData;

public class QueryNmpipeInfoResponse implements TransData{
	
	// data
	short outputBufferSize;
	short inputBufferSize;
	byte maximumInstance;
	byte currentInstance;
	byte pipeNameLength;
	String pipeName;
	public short getOutputBufferSize() {
		return outputBufferSize;
	}
	public void setOutputBufferSize(short outputBufferSize) {
		this.outputBufferSize = outputBufferSize;
	}
	public short getInputBufferSize() {
		return inputBufferSize;
	}
	public void setInputBufferSize(short inputBufferSize) {
		this.inputBufferSize = inputBufferSize;
	}
	public byte getMaximumInstance() {
		return maximumInstance;
	}
	public void setMaximumInstance(byte maximumInstance) {
		this.maximumInstance = maximumInstance;
	}
	public byte getCurrentInstance() {
		return currentInstance;
	}
	public void setCurrentInstance(byte currentInstance) {
		this.currentInstance = currentInstance;
	}
	public byte getPipeNameLength() {
		return pipeNameLength;
	}
	public void setPipeNameLength(byte pipeNameLength) {
		this.pipeNameLength = pipeNameLength;
	}
	public String getPipeName() {
		return pipeName;
	}
	public void setPipeName(String pipeName) {
		this.pipeName = pipeName;
	}
	@Override
	public String toString(){
		return String.format("");
	}
}
